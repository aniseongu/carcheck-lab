package edu.skku.map.carchecklab

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VehicleReviewActivity : AppCompatActivity() {

    private lateinit var textReviewTitle: TextView
    private lateinit var textAverageRating: TextView
    private lateinit var textMyReviewStatus: TextView
    private lateinit var textStar1: TextView
    private lateinit var textStar2: TextView
    private lateinit var textStar3: TextView
    private lateinit var textStar4: TextView
    private lateinit var textStar5: TextView
    private lateinit var editReviewComment: EditText
    private lateinit var layoutReviewList: LinearLayout
    private lateinit var buttonSubmitReview: Button
    private lateinit var buttonDeleteReview: Button

    private var year: Int = 2022
    private var make: String = "KIA"
    private var model: String = "EV6"
    private var rating: Int = 5
    private var vehicleKey: String = ""
    private var currentUserId: String = "guest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_review)
        FontHelper.applyToViewTree(this, window.decorView)

        textReviewTitle = findViewById(R.id.textReviewTitle)
        textAverageRating = findViewById(R.id.textAverageRating)
        textMyReviewStatus = findViewById(R.id.textMyReviewStatus)

        textStar1 = findViewById(R.id.textStar1)
        textStar2 = findViewById(R.id.textStar2)
        textStar3 = findViewById(R.id.textStar3)
        textStar4 = findViewById(R.id.textStar4)
        textStar5 = findViewById(R.id.textStar5)

        editReviewComment = findViewById(R.id.editReviewComment)
        layoutReviewList = findViewById(R.id.layoutReviewList)

        buttonSubmitReview = findViewById(R.id.buttonSubmitReview)
        buttonDeleteReview = findViewById(R.id.buttonDeleteReview)

        val buttonBackReview: Button = findViewById(R.id.buttonBackReview)

        buttonSubmitReview.backgroundTintList = null
        buttonDeleteReview.backgroundTintList = null
        buttonBackReview.backgroundTintList = null

        year = intent.getIntExtra(VehicleSelectActivity.EXT_YEAR, 2022)
        make = intent.getStringExtra(VehicleSelectActivity.EXT_MAKE) ?: "KIA"
        model = intent.getStringExtra(VehicleSelectActivity.EXT_MODEL) ?: "EV6"

        vehicleKey = ReviewManager.makeVehicleKey(year, make, model)
        currentUserId = ServerSessionManager.getCurrentUserId(this).ifEmpty { "guest" }

        textReviewTitle.text =
            year.toString() + " " + make + " " + model

        textStar1.setOnClickListener { setRating(1) }
        textStar2.setOnClickListener { setRating(2) }
        textStar3.setOnClickListener { setRating(3) }
        textStar4.setOnClickListener { setRating(4) }
        textStar5.setOnClickListener { setRating(5) }

        buttonSubmitReview.setOnClickListener {
            submitReview()
        }

        buttonDeleteReview.setOnClickListener {
            deleteMyReview()
        }

        buttonBackReview.setOnClickListener {
            finish()
        }

        loadMyReview()
        updateStarViews()
        refreshReviewList()
    }

    private fun loadMyReview() {
        val myReview = ReviewManager.getMyReview(this, vehicleKey, currentUserId)

        if (myReview == null) {
            textMyReviewStatus.text =
                "You have not reviewed this vehicle yet. One review per user is allowed."
            editReviewComment.setText("")
            rating = 5
            buttonSubmitReview.text = "Submit review"
            buttonDeleteReview.isEnabled = false
            buttonDeleteReview.alpha = 0.45f
        } else {
            textMyReviewStatus.text =
                "You already reviewed this vehicle. You can edit or delete your review."
            editReviewComment.setText(myReview.comment)
            rating = myReview.rating
            buttonSubmitReview.text = "Update review"
            buttonDeleteReview.isEnabled = true
            buttonDeleteReview.alpha = 1.0f
        }
    }

    private fun setRating(value: Int) {
        rating = value
        updateStarViews()
    }

    private fun updateStarViews() {
        val stars = arrayOf(textStar1, textStar2, textStar3, textStar4, textStar5)

        for (i in stars.indices) {
            val star = stars[i]

            if (i < rating) {
                star.text = "★"
                star.setTextColor(getColor(R.color.skku_gold_600))
            } else {
                star.text = "☆"
                star.setTextColor(getColor(R.color.text_muted))
            }
        }
    }

    private fun submitReview() {
        val comment = editReviewComment.text.toString().trim()

        if (comment.isEmpty()) {
            Toast.makeText(this, "Write a comment.", Toast.LENGTH_SHORT).show()
            return
        }

        val now = System.currentTimeMillis()

        val existingReview = ReviewManager.getMyReview(this, vehicleKey, currentUserId)

        val createdAt =
            if (existingReview == null) {
                now
            } else {
                existingReview.createdAt
            }

        val review = VehicleReview(
            currentUserId,
            vehicleKey,
            rating,
            comment,
            createdAt,
            now
        )

        ReviewManager.addOrUpdateReview(this, review)

        Toast.makeText(this, "Review saved.", Toast.LENGTH_SHORT).show()

        loadMyReview()
        refreshReviewList()
    }

    private fun deleteMyReview() {
        ReviewManager.deleteReview(this, vehicleKey, currentUserId)

        Toast.makeText(this, "Your review was deleted.", Toast.LENGTH_SHORT).show()

        rating = 5
        editReviewComment.setText("")
        updateStarViews()
        loadMyReview()
        refreshReviewList()
    }

    private fun refreshReviewList() {
        val reviews = ReviewManager.getReviewsForVehicle(this, vehicleKey)
        val average = ReviewManager.getAverageRating(this, vehicleKey)

        if (reviews.size == 0) {
            textAverageRating.text =
                "No ratings yet\nBe the first to review this vehicle."
        } else {
            textAverageRating.text =
                "Average rating " + String.format("%.1f", average) +
                        " / 5.0 · " + reviews.size.toString() + " reviews"
        }

        layoutReviewList.removeAllViews()

        for (i in reviews.size - 1 downTo 0) {
            addReviewCard(reviews[i])
        }
    }

    private fun addReviewCard(review: VehicleReview) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundResource(R.drawable.card_skk_white)
        card.setPadding(dp(16), dp(14), dp(16), dp(14))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, dp(12))
        card.layoutParams = params

        val topRow = LinearLayout(this)
        topRow.orientation = LinearLayout.HORIZONTAL
        topRow.gravity = Gravity.CENTER_VERTICAL

        val userText = TextView(this)
        userText.text = review.userId
        userText.textSize = 15f
        userText.typeface = Typeface.DEFAULT_BOLD
        userText.setTextColor(getColor(R.color.skku_green_800))

        val dateText = TextView(this)
        dateText.text = formatDate(review.updatedAt)
        dateText.textSize = 12f
        dateText.setTextColor(getColor(R.color.text_muted))
        dateText.gravity = Gravity.END

        val userParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        val dateParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        userText.layoutParams = userParams
        dateText.layoutParams = dateParams

        topRow.addView(userText)
        topRow.addView(dateText)

        val starText = TextView(this)
        starText.text = makeStars(review.rating) + "  " + review.rating.toString() + ".0"
        starText.textSize = 16f
        starText.typeface = Typeface.DEFAULT_BOLD
        starText.setTextColor(getColor(R.color.skku_gold_600))
        starText.setPadding(0, dp(8), 0, dp(4))

        val commentText = TextView(this)
        commentText.text = review.comment
        commentText.textSize = 15f
        commentText.setTextColor(getColor(R.color.text_primary))
        commentText.setLineSpacing(5f, 1.0f)

        val ownerHint = TextView(this)

        if (review.userId == currentUserId) {
            ownerHint.text = "My review · editable"
            ownerHint.textSize = 12f
            ownerHint.setTextColor(getColor(R.color.skku_green_800))
            ownerHint.setPadding(0, dp(8), 0, 0)
        } else {
            ownerHint.text = ""
        }

        card.addView(topRow)
        card.addView(starText)
        card.addView(commentText)

        if (ownerHint.text.isNotEmpty()) {
            card.addView(ownerHint)
        }

        layoutReviewList.addView(card)
    }

    private fun makeStars(value: Int): String {
        var result = ""

        for (i in 1..5) {
            result += if (i <= value) {
                "★"
            } else {
                "☆"
            }
        }

        return result
    }

    private fun formatDate(time: Long): String {
        if (time <= 0L) {
            return ""
        }

        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.US)
        return formatter.format(Date(time))
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}