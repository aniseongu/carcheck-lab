package edu.skku.map.carchecklab

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CarSearchActivity : AppCompatActivity() {

    private lateinit var editSearchCar: EditText
    private lateinit var textSearchHint: TextView
    private lateinit var textSelectedSearchCar: TextView
    private lateinit var layoutSearchResults: LinearLayout
    private lateinit var buttonOpenReport: Button
    private lateinit var buttonOpenComments: Button

    private val matchedCars: ArrayList<MarketCar> = ArrayList()
    private var selectedCar: MarketCar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_search)
        FontHelper.applyToViewTree(this, window.decorView)

        FontHelper.applyToViewTree(this, window.decorView)

        MarketData.init(this)

        editSearchCar = findViewById(R.id.editSearchCar)
        textSearchHint = findViewById(R.id.textSearchHint)
        textSelectedSearchCar = findViewById(R.id.textSelectedSearchCar)
        layoutSearchResults = findViewById(R.id.layoutSearchResults)
        buttonOpenReport = findViewById(R.id.buttonOpenReport)
        buttonOpenComments = findViewById(R.id.buttonOpenComments)

        val buttonSearchCar: Button = findViewById(R.id.buttonSearchCar)
        val buttonBackSearch: Button = findViewById(R.id.buttonBackSearch)

        buttonSearchCar.backgroundTintList = null
        buttonOpenReport.backgroundTintList = null
        buttonOpenComments.backgroundTintList = null
        buttonBackSearch.backgroundTintList = null

        textSearchHint.text =
            "Search by brand or model. Example: Hyundai, Kia, Tesla, Toyota, BMW, EV6, Camry."

        textSelectedSearchCar.text = "Select a vehicle from the list."

        buttonSearchCar.setOnClickListener {
            val query = editSearchCar.text.toString().trim()
            searchCars(query)
        }

        buttonOpenReport.setOnClickListener {
            val car = selectedCar

            if (car == null) {
                Toast.makeText(this, "Select a vehicle first.", Toast.LENGTH_SHORT).show()
            } else {
                openRiskDashboard(car.year, car.make, car.model, car.category)
            }
        }

        buttonOpenComments.setOnClickListener {
            val car = selectedCar

            if (car == null) {
                Toast.makeText(this, "Select a vehicle first.", Toast.LENGTH_SHORT).show()
            } else {
                openReview(car.year, car.make, car.model, car.category)
            }
        }

        buttonBackSearch.setOnClickListener {
            finish()
        }

        searchCars("")
    }

    private fun searchCars(rawQuery: String) {
        matchedCars.clear()
        layoutSearchResults.removeAllViews()
        selectedCar = null
        textSelectedSearchCar.text = "Select a vehicle from the list."

        val query = normalizeQuery(rawQuery)
        val cars = MarketData.getCars()

        for (i in 0 until cars.size) {
            val car = cars[i]

            val targetText =
                normalizeQuery(
                    car.year.toString() + " " +
                            car.make + " " +
                            car.model + " " +
                            car.category
                )

            if (query.isEmpty() || targetText.contains(query)) {
                matchedCars.add(car)
                addSearchResultCard(car)
            }
        }

        if (matchedCars.size == 0) {
            val textView = TextView(this)
            textView.text = "No result. Try Hyundai, Kia, Tesla, Toyota, BMW, EV6, Camry."
            textView.textSize = 15f
            textView.typeface = FontHelper.bodyMedium(this)
            textView.setTextColor(getColor(R.color.text_secondary))
            textView.setBackgroundResource(R.drawable.card_skk_soft)
            textView.setPadding(dp(16), dp(16), dp(16), dp(16))

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.setMargins(0, 0, 0, dp(12))
            textView.layoutParams = params

            layoutSearchResults.addView(textView)
        }
    }

    private fun addSearchResultCard(car: MarketCar) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.HORIZONTAL
        card.gravity = Gravity.CENTER_VERTICAL
        card.setBackgroundResource(R.drawable.card_skk_white)
        card.setPadding(dp(14), dp(12), dp(14), dp(12))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, dp(10))
        card.layoutParams = params

        val iconView = ImageView(this)
        iconView.setImageResource(getCategoryIcon(car.category))
        iconView.scaleType = ImageView.ScaleType.FIT_CENTER
        iconView.contentDescription = car.category

        val iconParams = LinearLayout.LayoutParams(
            dp(66),
            dp(66)
        )

        iconView.layoutParams = iconParams

        val textBox = LinearLayout(this)
        textBox.orientation = LinearLayout.VERTICAL
        textBox.setPadding(dp(14), 0, 0, 0)

        val title = TextView(this)
        title.text =
            car.year.toString() + " " +
                    car.make + " " +
                    car.model

        title.textSize = 17f
        title.typeface = FontHelper.dashboardBold(this)
        title.setTextColor(getColor(R.color.skku_green_950))

        val subtitle = TextView(this)
        subtitle.text =
            car.category +
                    " / $" + car.estimatedPrice.toString() +
                    " / Wallet " + car.walletScore.toString()

        subtitle.textSize = 13f
        subtitle.typeface = FontHelper.bodyRegular(this)
        subtitle.setTextColor(getColor(R.color.text_secondary))
        subtitle.setPadding(0, dp(4), 0, 0)

        val hint = TextView(this)
        hint.text = "Tap to select"
        hint.textSize = 12f
        hint.typeface = FontHelper.bodyMedium(this)
        hint.setTextColor(getColor(R.color.skku_gold_700))
        hint.setPadding(0, dp(6), 0, 0)

        textBox.addView(title)
        textBox.addView(subtitle)
        textBox.addView(hint)

        val textBoxParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )

        textBox.layoutParams = textBoxParams

        card.addView(iconView)
        card.addView(textBox)

        card.setOnClickListener {
            selectedCar = car

            textSelectedSearchCar.text =
                "Selected: " +
                        car.year.toString() + " " +
                        car.make + " " +
                        car.model +
                        "\n" +
                        car.category +
                        " / $" + car.estimatedPrice.toString()
        }

        layoutSearchResults.addView(card)
    }

    private fun normalizeQuery(text: String): String {
        var result = text.uppercase().trim()

        result = result.replace("현대", "HYUNDAI")
        result = result.replace("기아", "KIA")
        result = result.replace("제네시스", "GENESIS")
        result = result.replace("테슬라", "TESLA")
        result = result.replace("토요타", "TOYOTA")
        result = result.replace("도요타", "TOYOTA")
        result = result.replace("렉서스", "LEXUS")
        result = result.replace("혼다", "HONDA")
        result = result.replace("벤츠", "MERCEDES")
        result = result.replace("비엠", "BMW")
        result = result.replace("아우디", "AUDI")

        result = result.replace(" ", "")
        result = result.replace("-", "")
        result = result.replace("_", "")

        return result
    }

    private fun getCategoryIcon(category: String): Int {
        val upper = category.uppercase()

        if (upper.contains("EV")) {
            return R.drawable.vehicle_ev
        }

        if (upper.contains("TRUCK")) {
            return R.drawable.vehicle_truck
        }

        if (upper.contains("SUV")) {
            return R.drawable.vehicle_suv
        }

        if (upper.contains("MINIVAN") || upper.contains("VAN")) {
            return R.drawable.vehicle_suv
        }

        return R.drawable.vehicle_sedan
    }

    private fun openRiskDashboard(
        year: Int,
        make: String,
        model: String,
        category: String
    ) {
        val intent = Intent(this, RiskDashboardActivity::class.java)
        intent.putExtra(VehicleSelectActivity.EXT_YEAR, year)
        intent.putExtra(VehicleSelectActivity.EXT_MAKE, make)
        intent.putExtra(VehicleSelectActivity.EXT_MODEL, model)
        intent.putExtra(VehicleSelectActivity.EXT_CATEGORY, category)
        startActivity(intent)
    }

    private fun openReview(
        year: Int,
        make: String,
        model: String,
        category: String
    ) {
        val intent = Intent(this, VehicleReviewActivity::class.java)
        intent.putExtra(VehicleSelectActivity.EXT_YEAR, year)
        intent.putExtra(VehicleSelectActivity.EXT_MAKE, make)
        intent.putExtra(VehicleSelectActivity.EXT_MODEL, model)
        intent.putExtra(VehicleSelectActivity.EXT_CATEGORY, category)
        startActivity(intent)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}