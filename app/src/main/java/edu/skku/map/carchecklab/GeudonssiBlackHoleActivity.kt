package edu.skku.map.carchecklab

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class BlackHoleStep(
    var title: String,
    var priceText: String,
    var roastText: String,
    var warningText: String,
    var isFakeOverheat: Boolean
)

class GeudonssiBlackHoleActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_YEAR: String = "extra_blackhole_year"
        const val EXTRA_MAKE: String = "extra_blackhole_make"
        const val EXTRA_MODEL: String = "extra_blackhole_model"
        const val EXTRA_CATEGORY: String = "extra_blackhole_category"
        const val EXTRA_RISK_SCORE: String = "extra_blackhole_risk"
        const val EXTRA_GEUDONSSI_SCORE: String = "extra_blackhole_geudonssi"
        const val EXTRA_WALLET_SCORE: String = "extra_blackhole_wallet"
        const val EXTRA_PRIORITY: String = "extra_blackhole_priority"
    }

    private lateinit var layoutBlackHoleRoot: LinearLayout
    private lateinit var textStageBadge: TextView
    private lateinit var textDefendantCar: TextView
    private lateinit var textPrice: TextView
    private lateinit var textRoast: TextView
    private lateinit var textWarning: TextView
    private lateinit var textBiasComment: TextView
    private lateinit var textHistory: TextView
    private lateinit var progressMadness: ProgressBar
    private lateinit var buttonNextLoop: Button
    private lateinit var buttonPreviousLoop: Button
    private lateinit var buttonShareBlackHole: Button
    private lateinit var spinnerBiasLens: Spinner

    private var year: Int = 2022
    private var make: String = "KIA"
    private var model: String = "EV6"
    private var category: String = "Vehicle"
    private var riskScore: Int = 0
    private var budgetDefenseScore: Int = 0
    private var walletScore: Int = 0
    private var priority: String = "Safety Nerd"

    private var currentIndex: Int = 0
    private var selectedBias: String = "Neutral Mode"

    private val steps: ArrayList<BlackHoleStep> = ArrayList<BlackHoleStep>()
    private val historyLines: ArrayList<String> = ArrayList<String>()

    private val backgroundColors: Array<String> = arrayOf(
        "#F5F7FA",
        "#FFF7ED",
        "#FFEDD5",
        "#FED7AA",
        "#FDBA74",
        "#FB923C",
        "#EA580C",
        "#7F1D1D"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_geudonssi_black_hole)
        FontHelper.applyToViewTree(this, window.decorView)

        MarketData.init(this)

        layoutBlackHoleRoot = findViewById<LinearLayout>(R.id.layoutBlackHoleRoot)
        textStageBadge = findViewById<TextView>(R.id.textStageBadge)
        textDefendantCar = findViewById<TextView>(R.id.textDefendantCar)
        textPrice = findViewById<TextView>(R.id.textPrice)
        textRoast = findViewById<TextView>(R.id.textRoast)
        textWarning = findViewById<TextView>(R.id.textWarning)
        textBiasComment = findViewById<TextView>(R.id.textBiasComment)
        textHistory = findViewById<TextView>(R.id.textHistory)
        progressMadness = findViewById<ProgressBar>(R.id.progressMadness)
        buttonNextLoop = findViewById<Button>(R.id.buttonNextLoop)
        buttonPreviousLoop = findViewById<Button>(R.id.buttonPreviousLoop)
        buttonShareBlackHole = findViewById<Button>(R.id.buttonShareBlackHole)
        spinnerBiasLens = findViewById<Spinner>(R.id.spinnerBiasLens)

        year = intent.getIntExtra(EXTRA_YEAR, 2022)
        make = intent.getStringExtra(EXTRA_MAKE) ?: "KIA"
        model = intent.getStringExtra(EXTRA_MODEL) ?: "EV6"
        category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Vehicle"
        riskScore = intent.getIntExtra(EXTRA_RISK_SCORE, 0)
        budgetDefenseScore = intent.getIntExtra(EXTRA_GEUDONSSI_SCORE, 0)
        walletScore = intent.getIntExtra(EXTRA_WALLET_SCORE, 0)
        priority = intent.getStringExtra(EXTRA_PRIORITY) ?: "Safety Nerd"

        setupBiasSpinner()
        buildBlackHoleSteps()
        showCurrentStep()

        buttonNextLoop.setOnClickListener {
            if (isCurrentFakeOverheat()) {
                resetBlackHole()
            } else {
                goNextStep()
            }
        }

        buttonPreviousLoop.setOnClickListener {
            goPreviousStep()
        }

        buttonShareBlackHole.setOnClickListener {
            shareBlackHoleResult()
        }

        val buttonBackBlackHole: Button = findViewById<Button>(R.id.buttonBackBlackHole)
        buttonBackBlackHole.setOnClickListener {
            finish()
        }
    }

    private fun setupBiasSpinner() {
        val modes: Array<String> = arrayOf(
            "Neutral Mode",
            "Tesla Fan Mode",
            "K-Brand Pride Mode",
            "German Badge Mode",
            "Toyota Reliability Club"
        )

        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                modes
            )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerBiasLens.adapter = adapter

        val defaultIndex: Int = getAutoBiasIndex()
        selectedBias = modes[defaultIndex]
        spinnerBiasLens.setSelection(defaultIndex)

        spinnerBiasLens.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedBias = modes[position]
                    currentIndex = 0
                    historyLines.clear()
                    buildBlackHoleSteps()
                    showCurrentStep()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedBias = "Neutral Mode"
                }
            }
    }

    private fun getAutoBiasIndex(): Int {
        val upperMake: String = make.uppercase()

        if (upperMake.contains("TESLA")) return 1

        if (
            upperMake.contains("HYUNDAI") ||
            upperMake.contains("KIA") ||
            upperMake.contains("GENESIS")
        ) return 2

        if (
            upperMake.contains("BMW") ||
            upperMake.contains("MERCEDES") ||
            upperMake.contains("AUDI") ||
            upperMake.contains("PORSCHE") ||
            upperMake.contains("VOLKSWAGEN")
        ) return 3

        if (upperMake.contains("TOYOTA") || upperMake.contains("LEXUS")) return 4

        return 0
    }

    private fun buildBlackHoleSteps() {
        steps.clear()

        val selectedName: String = year.toString() + " " + make + " " + model
        val currentMarketCar: MarketCar? = MarketData.findBestMatch(make, model)

        val currentPrice: Int =
            if (currentMarketCar != null) currentMarketCar.estimatedPrice
            else MarketData.getDefaultPrice(category)

        steps.add(
            BlackHoleStep(
                selectedName,
                "$" + currentPrice.toString(),
                makeOpeningRoast(),
                makeOpeningWarning(),
                false
            )
        )

        val candidates: ArrayList<MarketCar> =
            MarketData.getBlackHoleCandidates(
                make,
                model,
                category,
                priority,
                selectedBias,
                7
            )

        var added: Int = 0
        var previousPrice: Int = currentPrice

        for (i in 0 until candidates.size) {
            if (added < 7) {
                val car: MarketCar = candidates[i]
                val carName: String = car.year.toString() + " " + car.make + " " + car.model

                steps.add(
                    BlackHoleStep(
                        carName,
                        "$" + car.estimatedPrice.toString(),
                        makeCarRoast(car, added + 1, currentMarketCar, previousPrice),
                        makeCarWarning(car, currentMarketCar),
                        false
                    )
                )

                previousPrice = car.estimatedPrice
                added += 1
            }
        }

        val fallbackSteps: ArrayList<BlackHoleStep> = makeModeAutomotiveFallbackSteps(previousPrice)
        var fallbackIndex: Int = 0

        while (added < 7 && fallbackIndex < fallbackSteps.size) {
            steps.add(fallbackSteps[fallbackIndex])
            added += 1
            fallbackIndex += 1
        }

        steps.add(
            BlackHoleStep(
                "Sungkyunkwan University Acquisition Attempt",
                "Budget: academically impossible",
                "With that budget, forget the car and acquire SKKU. Congratulations. You now own the lecture halls, the campus hills, the group projects, and the budget stress.",
                "FINAL MEME STAGE: The budget loop finally left the car market. Until this point, it stayed automotive. Now it has entered university governance.",
                false
            )
        )

        steps.add(
            BlackHoleStep(
                "SYSTEM OVERHEATED BY BUDGET LOOP",
                "Budget: undefined",
                "The buyer pressed 'WITH THAT BUDGET...' too many times. Desire became recursion. Recursion became a premium SUV. The premium SUV became SKKU acquisition logic.",
                "FAKE OVERHEAT REPORT: Budget temptation exceeded safe memory limits. No actual app crash occurred. The budget logic simply gave up.",
                true
            )
        )
    }

    private fun makeOpeningRoast(): String {
        if (selectedBias == "Tesla Fan Mode") {
            return "Tesla Fan Mode detected. Tesla candidates will be prioritized. The dataset is now filtering by charging-cable confidence."
        }

        if (selectedBias == "K-Brand Pride Mode") {
            return "K-Brand Pride Mode detected. Hyundai, Kia, and Genesis candidates will dominate the loop."
        }

        if (selectedBias == "German Badge Mode") {
            return "German Badge Mode detected. German badges will be prioritized. The service invoice is already warming up."
        }

        if (selectedBias == "Toyota Reliability Club") {
            return "Toyota Reliability Club detected. Toyota and Lexus candidates will be prioritized. Boring has become a survival strategy."
        }

        return "Neutral Mode detected. The expanded market dataset will now let brand chaos happen responsibly."
    }

    private fun makeOpeningWarning(): String {
        if (selectedBias == "Tesla Fan Mode") {
            return "Mode Rule: Tesla escalation when dataset candidates exist. Cybertruck-level temptation may appear near the end."
        }

        if (selectedBias == "K-Brand Pride Mode") {
            return "Mode Rule: Korean brands are favored. Recalls still count. Pride is not a warranty."
        }

        if (selectedBias == "German Badge Mode") {
            return "Mode Rule: German badges are favored. Hachagam rises. Wallet confidence decreases."
        }

        if (selectedBias == "Toyota Reliability Club") {
            return "Mode Rule: Toyota and Lexus are favored. Emotional excitement reduced. Survival probability increased."
        }

        return "Mode Rule: Price must keep rising. Sanity optional."
    }

    private fun makeCarRoast(
        car: MarketCar,
        stage: Int,
        currentCar: MarketCar?,
        previousPrice: Int
    ): String {
        val name: String = car.year.toString() + " " + car.make + " " + car.model
        val priceJump: Int = car.estimatedPrice - previousPrice

        val priceText: String =
            if (priceJump > 0) "Price escalation: +$" + priceJump.toString() + " from previous stage."
            else "Price escalation: suspicious. The loop is bending reality."

        val conditionText: String =
            if (currentCar != null) MarketData.makeImprovementSummary(car, currentCar, priority)
            else "Condition logic: Original vehicle was not found in the local dataset, so fallback comparison is used."

        val modeText: String =
            if (selectedBias == "Tesla Fan Mode") {
                "Tesla Fan logic says this is not only a purchase. It is a software-update lifestyle confession."
            } else if (selectedBias == "K-Brand Pride Mode") {
                "K-Brand Pride Mode grants local familiarity aura. Domestic confidence has entered the comparison table."
            } else if (selectedBias == "German Badge Mode") {
                "The badge whispers prestige. The service center whispers budget shock."
            } else if (selectedBias == "Toyota Reliability Club") {
                "The club approves. It is boring, reliable, and annoyingly hard to argue against."
            } else {
                "The expanded dataset found another way to make the budget climb."
            }

        val stageText: String =
            if (stage >= 5) {
                "This is how budgets disappear: one reasonable-looking upgrade at a time."
            } else {
                "With that budget, " + name + " appears."
            }

        return stageText + "\n\n" + modeText + "\n\n" + priceText + "\n" + conditionText
    }

    private fun makeCarWarning(
        car: MarketCar,
        currentCar: MarketCar?
    ): String {
        if (currentCar != null && !MarketData.isBetterThan(car, currentCar, priority)) {
            return "Warning: This candidate is not clearly better. The budget loop is now using price escalation as emotional evidence."
        }

        if (selectedBias == "Tesla Fan Mode") {
            return "Tesla Warning: Battery warranty, charging history, and tire wear must be checked."
        }

        if (selectedBias == "K-Brand Pride Mode") {
            return "K-Brand Warning: Familiar service networks help, but recalls still count."
        }

        if (selectedBias == "German Badge Mode") {
            return "German Badge Warning: Hachagam is high. Maintenance mercy is low."
        }

        if (selectedBias == "Toyota Reliability Club") {
            return "Reliability Warning: This may be the responsible answer. Boring, but effective."
        }

        if (car.walletScore < 50) {
            return "Wallet Warning: This option may turn fuel and service costs into a recurring headache."
        }

        if (car.hachagamScore >= 90) {
            return "Hachagam Warning: Visual appeal detected. Financial judgment may be impaired."
        }

        if (car.familyScore >= 90) {
            return "Family Logic Warning: This is painfully reasonable. The internet may not respect it, but your spine might."
        }

        if (car.badWeatherScore >= 90) {
            return "Weather Warning: Bad-weather confidence detected. Rainy-day excuses are now weaker."
        }

        return "Upgrade Warning: This candidate passed the expanded dataset filter. Your original budget is filing a complaint."
    }

    private fun makeModeAutomotiveFallbackSteps(lastDatasetPrice: Int): ArrayList<BlackHoleStep> {
        if (selectedBias == "Tesla Fan Mode") return makeTeslaFallbackSteps(lastDatasetPrice)
        if (selectedBias == "K-Brand Pride Mode") return makeKBrandFallbackSteps(lastDatasetPrice)
        if (selectedBias == "German Badge Mode") return makeGermanFallbackSteps(lastDatasetPrice)
        if (selectedBias == "Toyota Reliability Club") return makeToyotaFallbackSteps(lastDatasetPrice)
        return makeNeutralFallbackSteps(lastDatasetPrice)
    }

    private fun makeTeslaFallbackSteps(lastPrice: Int): ArrayList<BlackHoleStep> {
        val result: ArrayList<BlackHoleStep> = ArrayList<BlackHoleStep>()
        val price: Int = maxOf(lastPrice, 62000)

        result.add(
            BlackHoleStep(
                "Tesla Model S Plaid Keyboard Warrior Edition",
                "$" + (price + 18000).toString(),
                "With that budget, Model S Plaid appears. Acceleration is instant. Financial justification is still loading.",
                "Tesla fallback: Tesla-only rule preserved. Charging anxiety upgraded to premium.",
                false
            )
        )

        result.add(
            BlackHoleStep(
                "Tesla Cybertruck Stainless Steel Ego Container",
                "$" + (price + 48000).toString(),
                "With that budget, Cybertruck appears. It is not a truck. It is a geometric argument with wheels.",
                "Tesla final auto stage: The loop ends in Cybertruck because subtlety was apparently unavailable.",
                false
            )
        )

        return result
    }

    private fun makeKBrandFallbackSteps(lastPrice: Int): ArrayList<BlackHoleStep> {
        val result: ArrayList<BlackHoleStep> = ArrayList<BlackHoleStep>()
        val price: Int = maxOf(lastPrice, 52000)

        result.add(
            BlackHoleStep(
                "Genesis G90 Presidential Comfort Spec",
                "$" + (price + 18000).toString(),
                "With that budget, Genesis G90 appears. K-Brand Pride Mode is now wearing a suit and pretending this is responsible.",
                "K-Brand fallback: domestic-brand rule preserved. Pride now has rear-seat comfort.",
                false
            )
        )

        result.add(
            BlackHoleStep(
                "Hyundai N Vision 74 Dream That Never Discounts",
                "$" + (price + 42000).toString(),
                "With that budget, N Vision 74 appears. It may not be practical, but the budget loop has stopped listening to practicality.",
                "K-Brand final auto stage: The loop ends in Korean concept-car fantasy.",
                false
            )
        )

        return result
    }

    private fun makeGermanFallbackSteps(lastPrice: Int): ArrayList<BlackHoleStep> {
        val result: ArrayList<BlackHoleStep> = ArrayList<BlackHoleStep>()
        val price: Int = maxOf(lastPrice, 70000)

        result.add(
            BlackHoleStep(
                "Porsche 911 Maintenance Anxiety Package",
                "$" + (price + 25000).toString(),
                "With that budget, Porsche 911 appears. The car says dream. The service history says evidence.",
                "German fallback: badge rule preserved. Hachagam is rising faster than tuition.",
                false
            )
        )

        result.add(
            BlackHoleStep(
                "Mercedes-AMG G63 Brick-Shaped Invoice",
                "$" + (price + 75000).toString(),
                "With that budget, G63 appears. Now you are not driving. You are announcing municipal-level financial confidence.",
                "German final auto stage: The badge won. The wallet did not attend the hearing.",
                false
            )
        )

        return result
    }

    private fun makeToyotaFallbackSteps(lastPrice: Int): ArrayList<BlackHoleStep> {
        val result: ArrayList<BlackHoleStep> = ArrayList<BlackHoleStep>()
        val price: Int = maxOf(lastPrice, 48000)

        result.add(
            BlackHoleStep(
                "Lexus RX 500h Responsible Luxury Blob",
                "$" + (price + 18000).toString(),
                "With that budget, Lexus RX appears. It is quiet, sensible, and annoyingly difficult to roast.",
                "Toyota/Lexus fallback: reliability logic preserved. Fun has been reduced for survival.",
                false
            )
        )

        result.add(
            BlackHoleStep(
                "Lexus LFA Financial Mythology Edition",
                "$" + (price + 250000).toString(),
                "With that budget, Lexus LFA appears. Toyota Reliability Club has accidentally become a museum acquisition strategy.",
                "Toyota/Lexus final auto stage: It is still Toyota-adjacent. It is also budget-adjacent to catastrophe.",
                false
            )
        )

        return result
    }

    private fun makeNeutralFallbackSteps(lastPrice: Int): ArrayList<BlackHoleStep> {
        val result: ArrayList<BlackHoleStep> = ArrayList<BlackHoleStep>()
        val price: Int = maxOf(lastPrice, 80000)

        result.add(
            BlackHoleStep(
                "Used Porsche 911 With Suspiciously Low Mileage",
                "$" + (price + 25000).toString(),
                "With that budget, a used Porsche 911 appears. The car says dream. The maintenance history says courtroom evidence.",
                "Automotive overheat stage: Still car-related. Still technically possible. Absolutely not emotionally stable.",
                false
            )
        )

        result.add(
            BlackHoleStep(
                "McLaren Parking Basement Anxiety",
                "$" + (price + 160000).toString(),
                "With that budget, a McLaren appears. It is very fast, which is useful when escaping your own financial decisions.",
                "Automotive final stage: This is still technically a car. That is the only defense left.",
                false
            )
        )

        return result
    }

    private fun showCurrentStep() {
        if (steps.size == 0) return

        val step: BlackHoleStep = steps[currentIndex]

        val colorIndex: Int =
            if (currentIndex >= backgroundColors.size) backgroundColors.size - 1
            else currentIndex

        layoutBlackHoleRoot.setBackgroundColor(Color.parseColor(backgroundColors[colorIndex]))

        val madness: Int =
            if (steps.size <= 1) 0
            else (currentIndex * 100) / (steps.size - 1)

        progressMadness.progress = madness

        textStageBadge.text =
            "BUDGET LOOP STAGE " + currentIndex.toString() +
                    " / Madness " + madness.toString() + "%"

        textDefendantCar.text = step.title
        textPrice.text = step.priceText
        textRoast.text = step.roastText
        textWarning.text = step.warningText
        textBiasComment.text = makeBiasComment(step.title)

        updateHistory(step)
        updateButtonState()

        if (step.isFakeOverheat) {
            buttonNextLoop.text = "RESET THE LOOP"
            textDefendantCar.setTextColor(Color.parseColor("#FEE2E2"))
            textRoast.setTextColor(Color.parseColor("#FEE2E2"))
            textWarning.setTextColor(Color.parseColor("#FFFFFF"))
        } else {
            buttonNextLoop.text = "WITH THAT BUDGET..."
            textDefendantCar.setTextColor(Color.parseColor("#111827"))
            textRoast.setTextColor(Color.parseColor("#374151"))
            textWarning.setTextColor(Color.parseColor("#92400E"))
        }
    }

    private fun makeBiasComment(title: String): String {
        val upperTitle: String = title.uppercase()

        if (selectedBias == "Tesla Fan Mode") {
            if (upperTitle.contains("TESLA") || upperTitle.contains("MODEL") || upperTitle.contains("CYBERTRUCK")) {
                return "Bias Lens: Tesla Fan Mode. Tesla escalation is active."
            }
            return "Bias Lens: Tesla Fan Mode. Non-Tesla target detected only if the loop escaped into final meme territory."
        }

        if (selectedBias == "K-Brand Pride Mode") {
            if (upperTitle.contains("HYUNDAI") || upperTitle.contains("KIA") || upperTitle.contains("GENESIS") || upperTitle.contains("N VISION")) {
                return "Bias Lens: K-Brand Pride Mode. Korean-brand escalation is active."
            }
            return "Bias Lens: K-Brand Pride Mode. Non-Korean target detected only if the loop escaped cars entirely."
        }

        if (selectedBias == "German Badge Mode") {
            if (upperTitle.contains("BMW") || upperTitle.contains("MERCEDES") || upperTitle.contains("AUDI") || upperTitle.contains("PORSCHE") || upperTitle.contains("VOLKSWAGEN")) {
                return "Bias Lens: German Badge Mode. The badge is strong. The maintenance invoice is stronger."
            }
            return "Bias Lens: German Badge Mode. No German badge detected."
        }

        if (selectedBias == "Toyota Reliability Club") {
            if (upperTitle.contains("TOYOTA") || upperTitle.contains("LEXUS")) {
                return "Bias Lens: Toyota Reliability Club. The car is boring. Sadly, boring often wins."
            }
            return "Bias Lens: Toyota Reliability Club. Non-Toyota target detected only if the loop became irrational."
        }

        return "Bias Lens: Neutral Mode. Brand chaos allowed."
    }

    private fun updateButtonState() {
        buttonPreviousLoop.isEnabled = currentIndex > 0
        buttonPreviousLoop.alpha = if (currentIndex > 0) 1.0f else 0.45f
    }

    private fun updateHistory(step: BlackHoleStep) {
        if (historyLines.size == 0) {
            historyLines.add(step.title)
        } else {
            val last: String = historyLines[historyLines.size - 1]

            if (last != step.title) {
                historyLines.add(step.title)
            }
        }

        val builder: StringBuilder = StringBuilder()
        builder.append("Budget Loop History\n")

        for (i in 0 until historyLines.size) {
            builder.append((i + 1).toString())
            builder.append(". ")
            builder.append(historyLines[i])
            builder.append("\n")
        }

        textHistory.text = builder.toString()
    }

    private fun goNextStep() {
        if (currentIndex < steps.size - 1) {
            currentIndex += 1
            showCurrentStep()
        }
    }

    private fun goPreviousStep() {
        if (currentIndex > 0) {
            currentIndex -= 1

            while (historyLines.size > currentIndex + 1) {
                historyLines.removeAt(historyLines.size - 1)
            }

            showCurrentStep()
        }
    }

    private fun resetBlackHole() {
        currentIndex = 0
        historyLines.clear()
        showCurrentStep()
    }

    private fun isCurrentFakeOverheat(): Boolean {
        if (steps.size == 0) return false
        return steps[currentIndex].isFakeOverheat
    }

    private fun shareBlackHoleResult() {
        val shareText: String = makeShareText()

        val intent: Intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, "CarCheck Lab Budget Black Hole")
        intent.putExtra(Intent.EXTRA_TEXT, shareText)

        startActivity(Intent.createChooser(intent, "Share Budget Black Hole Result"))
    }

    private fun makeShareText(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("CarCheck Lab: Budget Black Hole\n\n")
        builder.append("Original Vehicle:\n")
        builder.append(year.toString())
        builder.append(" ")
        builder.append(make)
        builder.append(" ")
        builder.append(model)
        builder.append("\n\n")

        builder.append("Profile:\n")
        builder.append(priority)
        builder.append("\n\n")

        builder.append("Bias Lens:\n")
        builder.append(selectedBias)
        builder.append("\n\n")

        builder.append("Original Scores:\n")
        builder.append("- Risk Score: ")
        builder.append(riskScore.toString())
        builder.append("\n")
        builder.append("- Budget Defense: ")
        builder.append(budgetDefenseScore.toString())
        builder.append("\n")
        builder.append("- Wallet Score: ")
        builder.append(walletScore.toString())
        builder.append("\n\n")

        builder.append("Budget Loop Path:\n")

        for (i in 0 until historyLines.size) {
            builder.append((i + 1).toString())
            builder.append(". ")
            builder.append(historyLines[i])
            builder.append("\n")
        }

        builder.append("\nFinal Verdict:\n")
        builder.append("The expanded market dataset kept raising the price through car candidates first. Then it collapsed into SKKU acquisition logic at the final stage.\n\n")
        builder.append("Generated by CarCheck Lab")

        return builder.toString()
    }
}