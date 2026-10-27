package edu.skku.map.carchecklab

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DealerCoachActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_VEHICLE_NAME: String = "extra_dealer_vehicle"
        const val EXTRA_CATEGORY: String = "extra_dealer_category"
        const val EXTRA_SCORE: String = "extra_dealer_score"
        const val EXTRA_GRADE: String = "extra_dealer_grade"
        const val EXTRA_RECALL: String = "extra_dealer_recall"
        const val EXTRA_COMPLAINT: String = "extra_dealer_complaint"
        const val EXTRA_SAFETY: String = "extra_dealer_safety"
        const val EXTRA_PRIORITY: String = "extra_dealer_priority"
    }

    private lateinit var layoutChatMessages: LinearLayout
    private lateinit var editCoachQuestion: EditText

    private var vehicleName: String = ""
    private var category: String = ""
    private var score: Int = 0
    private var grade: String = ""
    private var recall: Int = 0
    private var complaint: Int = 0
    private var safety: String = ""
    private var priority: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dealer_coach)
        FontHelper.applyToViewTree(this, window.decorView)

        vehicleName = intent.getStringExtra(EXTRA_VEHICLE_NAME) ?: "Unknown Vehicle"
        category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Vehicle"
        score = intent.getIntExtra(EXTRA_SCORE, 0)
        grade = intent.getStringExtra(EXTRA_GRADE) ?: "UNKNOWN"
        recall = intent.getIntExtra(EXTRA_RECALL, 0)
        complaint = intent.getIntExtra(EXTRA_COMPLAINT, 0)
        safety = intent.getStringExtra(EXTRA_SAFETY) ?: "Not Rated"
        priority = intent.getStringExtra(EXTRA_PRIORITY) ?: "Safety Nerd"

        val textCoachVehicle: TextView = findViewById(R.id.textCoachVehicle)
        layoutChatMessages = findViewById(R.id.layoutChatMessages)
        editCoachQuestion = findViewById(R.id.editCoachQuestion)

        val buttonRecall: Button = findViewById(R.id.buttonRecall)
        val buttonFuel: Button = findViewById(R.id.buttonFuel)
        val buttonWeather: Button = findViewById(R.id.buttonWeather)
        val buttonPrice: Button = findViewById(R.id.buttonPrice)
        val buttonBattery: Button = findViewById(R.id.buttonBattery)
        val buttonSendCoach: Button = findViewById(R.id.buttonSendCoach)
        val buttonBackCoach: Button = findViewById(R.id.buttonBackCoach)

        buttonRecall.backgroundTintList = null
        buttonFuel.backgroundTintList = null
        buttonWeather.backgroundTintList = null
        buttonPrice.backgroundTintList = null
        buttonBattery.backgroundTintList = null
        buttonSendCoach.backgroundTintList = null
        buttonBackCoach.backgroundTintList = null

        textCoachVehicle.text =
            vehicleName +
                    "\nScore: " + score.toString() + " / " + grade +
                    "\nRecall: " + recall.toString() +
                    " / Complaint: " + complaint.toString() +
                    " / Safety: " + safety +
                    "\nProfile: " + priority

        addCoachMessage(
            "Start",
            "Ask about recall, price, fuel, battery, warranty, or weather. I will turn it into dealer questions."
        )

        buttonRecall.setOnClickListener {
            addUserMessage("Recall")
            addCoachMessage("Recall", makeReply("recall"))
        }

        buttonFuel.setOnClickListener {
            addUserMessage("Fuel")
            addCoachMessage("Fuel", makeReply("fuel"))
        }

        buttonWeather.setOnClickListener {
            addUserMessage("Weather")
            addCoachMessage("Weather", makeReply("weather"))
        }

        buttonPrice.setOnClickListener {
            addUserMessage("Price")
            addCoachMessage("Price", makeReply("price"))
        }

        buttonBattery.setOnClickListener {
            addUserMessage("Battery")
            addCoachMessage("Battery", makeReply("battery"))
        }

        buttonSendCoach.setOnClickListener {
            val question: String = editCoachQuestion.text.toString().trim()

            if (question.isNotEmpty()) {
                addUserMessage(question)
                addCoachMessage("Custom", makeReply(question))
                editCoachQuestion.setText("")
            }
        }

        buttonBackCoach.setOnClickListener {
            finish()
        }
    }

    private fun makeReply(input: String): String {
        val lower: String = input.lowercase()

        if (lower.contains("recall")) {
            if (recall > 0) {
                return "Ask the dealer to show proof that all recalls were completed. Do not accept only verbal confirmation."
            }

            return "No recall count is currently loaded, but still ask for service records and title status."
        }

        if (lower.contains("price") || lower.contains("budget")) {
            return "Ask why this price is justified compared with similar vehicles. Also ask for accident history, tire condition, and service records before negotiating."
        }

        if (lower.contains("fuel") || lower.contains("wallet")) {
            return "Ask for real maintenance cost, recent fuel economy, tire replacement cost, and ownership records. Wallet score matters after purchase, not only at purchase."
        }

        if (lower.contains("battery") || lower.contains("ev")) {
            if (category.uppercase().contains("EV")) {
                return "For EVs, ask about battery warranty, charging history, software updates, range degradation, and tire wear."
            }

            return "This is not marked as an EV, but still ask about battery health, alternator, and electrical issues."
        }

        if (lower.contains("weather") || lower.contains("rain") || lower.contains("snow")) {
            return "Ask about tire condition, brake condition, wiper condition, underbody rust, and how the car behaves in rain or bad roads."
        }

        if (complaint >= 30) {
            return "Complaint volume is high. Ask why this model has many complaints and request maintenance proof before trusting the listing."
        }

        return "Ask for maintenance history, accident record, title status, inspection result, and proof of recall completion."
    }

    private fun addUserMessage(message: String) {
        val wrapper = LinearLayout(this)
        wrapper.orientation = LinearLayout.VERTICAL
        wrapper.gravity = Gravity.END

        val bubble = TextView(this)
        bubble.text = "You · " + message
        bubble.textSize = 15f
        bubble.setTextColor(getColor(R.color.white))
        bubble.setBackgroundResource(R.drawable.chat_bubble_user)
        bubble.setPadding(18, 12, 18, 12)

        val bubbleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        bubbleParams.setMargins(70, 8, 0, 8)
        bubble.layoutParams = bubbleParams

        wrapper.addView(bubble)
        layoutChatMessages.addView(wrapper)
    }

    private fun addCoachMessage(topic: String, message: String) {
        val wrapper = LinearLayout(this)
        wrapper.orientation = LinearLayout.VERTICAL
        wrapper.gravity = Gravity.START

        val bubble = TextView(this)
        bubble.text = "Dealer Coach · " + topic + "\n" + message
        bubble.textSize = 15f
        bubble.setTextColor(getColor(R.color.text_primary))
        bubble.setBackgroundResource(R.drawable.chat_bubble_coach)
        bubble.setPadding(18, 12, 18, 12)
        bubble.setLineSpacing(4f, 1.0f)

        val bubbleParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        bubbleParams.setMargins(0, 8, 70, 8)
        bubble.layoutParams = bubbleParams

        wrapper.addView(bubble)
        layoutChatMessages.addView(wrapper)
    }
}