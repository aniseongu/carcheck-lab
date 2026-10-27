package edu.skku.map.carchecklab

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class DashboardData(
    var recallCount: Int,
    var complaintCount: Int,
    var safetyRating: String,
    var recallIssues: ArrayList<Issue>,
    var complaintIssues: ArrayList<Issue>
)

class RiskDashboardActivity : AppCompatActivity() {

    companion object {
        var issueList: ArrayList<Issue> = ArrayList<Issue>()
    }

    private var currentYear: Int = 2022
    private var currentMake: String = "KIA"
    private var currentModel: String = "EV6"
    private var currentCategory: String = "Vehicle"

    private var currentVehicleName: String = ""
    private var currentRecallCount: Int = 0
    private var currentComplaintCount: Int = 0
    private var currentSafetyRating: String = "Not Rated"
    private var currentScore: Int = 0
    private var currentGrade: String = ""
    private var currentPriority: String = "Safety Nerd"
    private var currentBudgetDefenseScore: Int = 0
    private var currentWalletScore: Int = 0

    private var currentAlternativeFocus: String = "Auto by profile"

    private lateinit var textAverageRatingView: TextView
    private lateinit var textAlternativeFocus: TextView
    private lateinit var layoutAlternativeList: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_risk_dashboard)

        try {
            FontHelper.applyToViewTree(this, window.decorView)
        } catch (e: Exception) {
        }

        MarketData.init(this)
        issueList.clear()

        currentYear = intent.getIntExtra(VehicleSelectActivity.EXT_YEAR, 2022)
        currentMake = intent.getStringExtra(VehicleSelectActivity.EXT_MAKE) ?: "KIA"
        currentModel = intent.getStringExtra(VehicleSelectActivity.EXT_MODEL) ?: "EV6"
        currentCategory = intent.getStringExtra(VehicleSelectActivity.EXT_CATEGORY) ?: "Vehicle"

        currentVehicleName =
            currentYear.toString() + " " + currentMake + " " + currentModel

        val city: String = UserProfileManager.getCity(this)
        currentPriority = UserProfileManager.getPriority(this)
        val currentUserId: String = ServerSessionManager.getCurrentUserId(this)

        val textSelectedVehicle: TextView = findViewById(R.id.textSelectedVehicle)
        val textProfileInfo: TextView = findViewById(R.id.textProfileInfo)

        textAverageRatingView = findViewById(R.id.textAverageRating)
        val buttonRateFromDashboard: Button = findViewById(R.id.buttonRateFromDashboard)

        val textLoading: TextView = findViewById(R.id.textLoading)
        val textFinalVerdict: TextView = findViewById(R.id.textFinalVerdict)
        val textScoreGuide: TextView = findViewById(R.id.textScoreGuide)

        val circleRiskScore: CircularScoreView = findViewById(R.id.circleRiskScore)
        val textRiskGrade: TextView = findViewById(R.id.textRiskGrade)
        val textRiskComment: TextView = findViewById(R.id.textRiskComment)

        val textWalletScore: TextView = findViewById(R.id.textWalletScore)
        val progressWallet: ProgressBar = findViewById(R.id.progressWallet)

        val textBudgetDefenseScore: TextView = findViewById(R.id.textBudgetDefenseScore)
        val progressBudgetDefense: ProgressBar = findViewById(R.id.progressBudgetDefense)

        val textHachagamScore: TextView = findViewById(R.id.textHachagamScore)
        val progressHachagam: ProgressBar = findViewById(R.id.progressHachagam)

        val textPracticalityScore: TextView = findViewById(R.id.textPracticalityScore)
        val progressPracticality: ProgressBar = findViewById(R.id.progressPracticality)

        val textCleanDataScore: TextView = findViewById(R.id.textCleanDataScore)
        val progressCleanData: ProgressBar = findViewById(R.id.progressCleanData)

        textAlternativeFocus = findViewById(R.id.textAlternativeFocus)
        val buttonEditAlternativeFocus: Button = findViewById(R.id.buttonEditAlternativeFocus)
        layoutAlternativeList = findViewById(R.id.layoutAlternativeList)

        val textRecallCount: TextView = findViewById(R.id.textRecallCount)
        val textComplaintCount: TextView = findViewById(R.id.textComplaintCount)
        val textSafetyRating: TextView = findViewById(R.id.textSafetyRating)

        val textWeatherMode: TextView = findViewById(R.id.textWeatherMode)
        val textWeatherAdvice: TextView = findViewById(R.id.textWeatherAdvice)
        val textWeatherColor: TextView = findViewById(R.id.textWeatherColor)

        val buttonDetails: Button = findViewById(R.id.buttonDetails)
        val buttonSave: Button = findViewById(R.id.buttonSave)
        val buttonDealerCoach: Button = findViewById(R.id.buttonDealerCoach)
        val buttonOpenBlackHole: Button = findViewById(R.id.buttonOpenBlackHole)
        val buttonBackHome: Button = findViewById(R.id.buttonBackHome)

        buttonRateFromDashboard.backgroundTintList = null
        buttonEditAlternativeFocus.backgroundTintList = null
        buttonDetails.backgroundTintList = null
        buttonSave.backgroundTintList = null
        buttonDealerCoach.backgroundTintList = null
        buttonOpenBlackHole.backgroundTintList = null
        buttonBackHome.backgroundTintList = null

        currentAlternativeFocus = loadAlternativeFocus()

        textSelectedVehicle.text = currentVehicleName + " / " + currentCategory

        textProfileInfo.text =
            "User: " + if (currentUserId.isEmpty()) "Guest" else currentUserId +
                    "\nProfile: " + currentPriority +
                    " / Region: " + city

        textScoreGuide.text =
            "Higher is better. Green = Good, Orange = Caution, Red = Weak."

        updateAverageRating()
        updateAlternativeFocusText()

        buttonRateFromDashboard.setOnClickListener {
            openReview(currentYear, currentMake, currentModel, currentCategory)
        }

        buttonEditAlternativeFocus.setOnClickListener {
            showAlternativeFocusDialog()
        }

        buttonDetails.isEnabled = false
        buttonSave.isEnabled = false
        buttonDealerCoach.isEnabled = false
        buttonOpenBlackHole.isEnabled = false

        buttonDetails.setOnClickListener {
            val intent = Intent(this, IssueDetailActivity::class.java)
            startActivity(intent)
        }

        buttonSave.setOnClickListener {
            val savedResult = SavedCarResult(
                currentVehicleName,
                currentScore,
                currentGrade,
                currentRecallCount,
                currentComplaintCount,
                currentSafetyRating
            )

            SavedCarsActivity.savedCars.add(savedResult)
            Toast.makeText(this, "Report saved.", Toast.LENGTH_SHORT).show()
        }

        buttonDealerCoach.setOnClickListener {
            val intent = Intent(this, DealerCoachActivity::class.java)
            intent.putExtra(DealerCoachActivity.EXTRA_VEHICLE_NAME, currentVehicleName)
            intent.putExtra(DealerCoachActivity.EXTRA_CATEGORY, currentCategory)
            intent.putExtra(DealerCoachActivity.EXTRA_SCORE, currentScore)
            intent.putExtra(DealerCoachActivity.EXTRA_GRADE, currentGrade)
            intent.putExtra(DealerCoachActivity.EXTRA_RECALL, currentRecallCount)
            intent.putExtra(DealerCoachActivity.EXTRA_COMPLAINT, currentComplaintCount)
            intent.putExtra(DealerCoachActivity.EXTRA_SAFETY, currentSafetyRating)
            intent.putExtra(DealerCoachActivity.EXTRA_PRIORITY, currentPriority)
            startActivity(intent)
        }

        buttonOpenBlackHole.setOnClickListener {
            val intent = Intent(this, GeudonssiBlackHoleActivity::class.java)
            intent.putExtra(GeudonssiBlackHoleActivity.EXTRA_YEAR, currentYear)
            intent.putExtra(GeudonssiBlackHoleActivity.EXTRA_MAKE, currentMake)
            intent.putExtra(GeudonssiBlackHoleActivity.EXTRA_MODEL, currentModel)
            intent.putExtra(GeudonssiBlackHoleActivity.EXTRA_CATEGORY, currentCategory)
            intent.putExtra(GeudonssiBlackHoleActivity.EXTRA_RISK_SCORE, currentScore)
            intent.putExtra(GeudonssiBlackHoleActivity.EXTRA_GEUDONSSI_SCORE, currentBudgetDefenseScore)
            intent.putExtra(GeudonssiBlackHoleActivity.EXTRA_WALLET_SCORE, currentWalletScore)
            intent.putExtra(GeudonssiBlackHoleActivity.EXTRA_PRIORITY, currentPriority)
            startActivity(intent)
        }

        buttonBackHome.setOnClickListener {
            finish()
        }

        loadDashboardData(
            city,
            textLoading,
            textFinalVerdict,
            circleRiskScore,
            textRiskGrade,
            textRiskComment,
            textWalletScore,
            progressWallet,
            textBudgetDefenseScore,
            progressBudgetDefense,
            textHachagamScore,
            progressHachagam,
            textPracticalityScore,
            progressPracticality,
            textCleanDataScore,
            progressCleanData,
            textRecallCount,
            textComplaintCount,
            textSafetyRating,
            textWeatherMode,
            textWeatherAdvice,
            textWeatherColor,
            buttonDetails,
            buttonSave,
            buttonDealerCoach,
            buttonOpenBlackHole
        )
    }

    override fun onResume() {
        super.onResume()

        if (::textAverageRatingView.isInitialized) {
            updateAverageRating()
        }
    }

    private fun updateAverageRating() {
        val key = ReviewManager.makeVehicleKey(currentYear, currentMake, currentModel)
        val count = ReviewManager.getReviewCount(this, key)
        val average = ReviewManager.getAverageRating(this, key)

        if (count == 0) {
            textAverageRatingView.text =
                "Rating  ··· / 5.0\nNo user rating yet."
        } else {
            textAverageRatingView.text =
                "Rating  " + String.format("%.1f", average) +
                        " / 5.0\n" + count.toString() + " community reviews"
        }
    }

    private fun updateAlternativeFocusText() {
        textAlternativeFocus.text = "Focus: " + currentAlternativeFocus
    }

    private fun showAlternativeFocusDialog() {
        val options = arrayOf(
            "Auto by profile",
            "Lowest price",
            "Wallet saver",
            "Safety evidence",
            "K-Brand only",
            "EV candidates",
            "Family practicality",
            "Balanced evidence"
        )

        AlertDialog.Builder(this)
            .setTitle("Choose alternative focus")
            .setItems(options) { dialog, which ->
                currentAlternativeFocus = options[which]
                saveAlternativeFocus(currentAlternativeFocus)
                updateAlternativeFocusText()
                renderAlternatives()
            }
            .show()
    }

    private fun loadAlternativeFocus(): String {
        val prefs = getSharedPreferences("carcheck_alternative_focus", MODE_PRIVATE)
        return prefs.getString(getCurrentVehicleKey(), "Auto by profile") ?: "Auto by profile"
    }

    private fun saveAlternativeFocus(focus: String) {
        val prefs = getSharedPreferences("carcheck_alternative_focus", MODE_PRIVATE)
        prefs.edit().putString(getCurrentVehicleKey(), focus).apply()
    }

    private fun getCurrentVehicleKey(): String {
        return ReviewManager.makeVehicleKey(currentYear, currentMake, currentModel)
    }

    private fun loadDashboardData(
        city: String,
        textLoading: TextView,
        textFinalVerdict: TextView,
        circleRiskScore: CircularScoreView,
        textRiskGrade: TextView,
        textRiskComment: TextView,
        textWalletScore: TextView,
        progressWallet: ProgressBar,
        textBudgetDefenseScore: TextView,
        progressBudgetDefense: ProgressBar,
        textHachagamScore: TextView,
        progressHachagam: ProgressBar,
        textPracticalityScore: TextView,
        progressPracticality: ProgressBar,
        textCleanDataScore: TextView,
        progressCleanData: ProgressBar,
        textRecallCount: TextView,
        textComplaintCount: TextView,
        textSafetyRating: TextView,
        textWeatherMode: TextView,
        textWeatherAdvice: TextView,
        textWeatherColor: TextView,
        buttonDetails: Button,
        buttonSave: Button,
        buttonDealerCoach: Button,
        buttonOpenBlackHole: Button
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            textLoading.text = "Loading vehicle data..."

            circleRiskScore.setScoreValue(
                0,
                "Risk",
                "Loading",
                Color.parseColor("#0B5A42")
            )

            val task: Deferred<DashboardData> = CoroutineScope(Dispatchers.IO).async {
                val nhtsaClient = NhtsaClient()

                val recallResponse = nhtsaClient.getRecalls(currentYear, currentMake, currentModel)
                val complaintResponse = nhtsaClient.getComplaints(currentYear, currentMake, currentModel)
                val safetyRating = nhtsaClient.getSafetyRating(currentYear, currentMake, currentModel)

                val recallItems: ArrayList<RecallItem> =
                    recallResponse.results ?: ArrayList<RecallItem>()

                val complaintItems: ArrayList<ComplaintItem> =
                    complaintResponse.results ?: ArrayList<ComplaintItem>()

                DashboardData(
                    recallItems.size,
                    complaintItems.size,
                    safetyRating,
                    makeRecallIssues(recallItems),
                    makeComplaintIssues(complaintItems)
                )
            }

            try {
                val data = task.await()

                currentRecallCount = data.recallCount
                currentComplaintCount = data.complaintCount
                currentSafetyRating = data.safetyRating

                val marketCar = MarketData.findBestMatch(currentMake, currentModel)
                val localSafetyScore = marketCar?.safetyScore

                currentScore = RiskCalculator.calculateScore(
                    data.recallCount,
                    data.complaintCount,
                    data.safetyRating,
                    localSafetyScore
                )

                currentGrade = RiskCalculator.getGrade(currentScore)

                val comment = RiskCalculator.getComment(
                    currentScore,
                    data.recallCount,
                    data.complaintCount,
                    data.safetyRating
                )

                textLoading.text = "Vehicle data loaded."

                circleRiskScore.setScoreValue(
                    currentScore,
                    "Risk",
                    getScoreLabel(currentScore),
                    getScoreColor(currentScore)
                )

                textRiskGrade.text = currentGrade
                textRiskComment.text = comment
                textRiskGrade.setTextColor(getScoreColor(currentScore))

                textRecallCount.text = "Recall Count: " + data.recallCount.toString()
                textComplaintCount.text = "Complaint Count: " + data.complaintCount.toString()
                textSafetyRating.text =
                    RiskCalculator.makeSafetyDisplay(data.safetyRating, localSafetyScore)

                val memeResult = MemeAnalyzer.analyze(
                    currentMake,
                    currentModel,
                    currentCategory,
                    currentScore,
                    data.recallCount,
                    data.complaintCount,
                    data.safetyRating,
                    currentPriority
                )

                currentBudgetDefenseScore = memeResult.geudonssiScore
                currentWalletScore = marketCar?.walletScore ?: 60

                setScoreRow(textWalletScore, progressWallet, "Wallet", currentWalletScore)
                setScoreRow(textBudgetDefenseScore, progressBudgetDefense, "Budget Defense", memeResult.geudonssiScore)
                setScoreRow(textHachagamScore, progressHachagam, "Hachagam", memeResult.hachagamScore)
                setScoreRow(textPracticalityScore, progressPracticality, "Practicality", memeResult.practicalityScore)
                setScoreRow(textCleanDataScore, progressCleanData, "Clean Data", memeResult.cleanDataScore)

                val verdictText = makeFinalVerdictWithWallet(
                    currentScore,
                    currentBudgetDefenseScore,
                    currentWalletScore,
                    currentPriority
                )

                textFinalVerdict.text = verdictText
                applyVerdictStyle(textFinalVerdict, currentScore)

                renderAlternatives()

                issueList.clear()
                issueList.addAll(data.recallIssues)
                issueList.addAll(data.complaintIssues)

                if (issueList.size == 0) {
                    issueList.add(
                        Issue(
                            "EMPTY",
                            "No issue sample found",
                            "This vehicle has no recall or complaint sample in this API response."
                        )
                    )
                }

                buttonDetails.isEnabled = true
                buttonSave.isEnabled = true
                buttonDealerCoach.isEnabled = true
                buttonOpenBlackHole.isEnabled = true

                loadWeatherAdvice(
                    city,
                    textWeatherMode,
                    textWeatherAdvice,
                    textWeatherColor
                )

            } catch (e: Exception) {
                textLoading.text = "Failed to load NHTSA data."

                textFinalVerdict.text =
                    "⚠️ NETWORK ERROR\nVehicle data could not be loaded. Please check connection."
                textFinalVerdict.setBackgroundResource(R.drawable.verdict_high_bg)
                textFinalVerdict.setTextColor(getColor(R.color.white))

                circleRiskScore.setScoreValue(
                    0,
                    "Risk",
                    "Error",
                    Color.parseColor("#DC2626")
                )

                textRiskGrade.text = "NETWORK ERROR"
                textRiskGrade.setTextColor(Color.parseColor("#DC2626"))
                textRiskComment.text = "Please check internet connection and try again."

                issueList.clear()
                issueList.add(
                    Issue(
                        "ERROR",
                        "Network error",
                        "NHTSA API data could not be loaded."
                    )
                )

                buttonDetails.isEnabled = true
                buttonSave.isEnabled = false
                buttonDealerCoach.isEnabled = false
                buttonOpenBlackHole.isEnabled = false
            }
        }
    }

    private fun renderAlternatives() {
        layoutAlternativeList.removeAllViews()

        val candidates = ArrayList<MarketCar>()
        val allCars = MarketData.getCars()

        for (i in 0 until allCars.size) {
            val car = allCars[i]

            if (!isSameVehicle(car) && matchesAlternativeFocus(car)) {
                candidates.add(car)
            }
        }

        if (candidates.size == 0) {
            for (i in 0 until allCars.size) {
                val car = allCars[i]

                if (!isSameVehicle(car)) {
                    candidates.add(car)
                }
            }
        }

        candidates.sortWith { a, b ->
            scoreAlternativeForFocus(b).compareTo(scoreAlternativeForFocus(a))
        }

        var count = 0

        for (i in 0 until candidates.size) {
            if (count < 3) {
                addAlternativeCard(count + 1, candidates[i])
                count += 1
            }
        }

        if (count == 0) {
            val textView = TextView(this)
            textView.text = "No alternative candidate found."
            textView.textSize = 14f
            textView.setTextColor(getColor(R.color.text_secondary))
            layoutAlternativeList.addView(textView)
        }
    }

    private fun matchesAlternativeFocus(car: MarketCar): Boolean {
        val make = car.make.uppercase()
        val category = car.category.uppercase()

        if (currentAlternativeFocus == "K-Brand only") {
            return make == "HYUNDAI" || make == "KIA" || make == "GENESIS"
        }

        if (currentAlternativeFocus == "EV candidates") {
            return category.contains("EV") || category.contains("ELECTRIC")
        }

        if (currentAlternativeFocus == "Family practicality") {
            return category.contains("SUV") ||
                    category.contains("VAN") ||
                    category.contains("MINIVAN")
        }

        return true
    }

    private fun scoreAlternativeForFocus(car: MarketCar): Int {
        val base = MarketData.calculateDecisionScore(car, currentPriority)
        val category = car.category.uppercase()
        val make = car.make.uppercase()

        if (currentAlternativeFocus == "Lowest price") {
            val pricePenalty = car.estimatedPrice / 500
            return 120 + car.walletScore - pricePenalty
        }

        if (currentAlternativeFocus == "Wallet saver") {
            val pricePenalty = car.estimatedPrice / 1000
            return base + car.walletScore - pricePenalty
        }

        if (currentAlternativeFocus == "Safety evidence") {
            return base + car.safetyScore
        }

        if (currentAlternativeFocus == "K-Brand only") {
            val brandBonus =
                if (make == "HYUNDAI" || make == "KIA" || make == "GENESIS") {
                    40
                } else {
                    0
                }

            return base + brandBonus
        }

        if (currentAlternativeFocus == "EV candidates") {
            val evBonus =
                if (category.contains("EV") || category.contains("ELECTRIC")) {
                    40
                } else {
                    0
                }

            return base + evBonus + car.walletScore
        }

        if (currentAlternativeFocus == "Family practicality") {
            val familyBonus =
                if (
                    category.contains("SUV") ||
                    category.contains("VAN") ||
                    category.contains("MINIVAN")
                ) {
                    40
                } else {
                    0
                }

            return base + familyBonus + car.safetyScore
        }

        if (currentAlternativeFocus == "Balanced evidence") {
            return base + car.safetyScore / 2 + car.walletScore / 2
        }

        return base
    }

    private fun isSameVehicle(car: MarketCar): Boolean {
        return car.make.uppercase() == currentMake.uppercase() &&
                car.model.uppercase() == currentModel.uppercase()
    }

    private fun addAlternativeCard(rank: Int, car: MarketCar) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundResource(R.drawable.card_skk_soft)
        card.setPadding(dp(14), dp(12), dp(14), dp(12))

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, dp(10))
        card.layoutParams = params

        val title = TextView(this)
        title.text =
            rank.toString() + ". " +
                    car.year.toString() + " " +
                    car.make + " " + car.model

        title.textSize = 16f
        title.typeface = Typeface.DEFAULT_BOLD
        title.setTextColor(getColor(R.color.skku_green_950))

        val decision = scoreAlternativeForFocus(car)

        val subtitle = TextView(this)
        subtitle.text =
            "$" + car.estimatedPrice.toString() +
                    " / Decision " + decision.toString() +
                    " / " + car.category +
                    "\nTap to open this vehicle's risk report."

        subtitle.textSize = 13f
        subtitle.setTextColor(getColor(R.color.text_secondary))
        subtitle.setLineSpacing(4f, 1.0f)
        subtitle.setPadding(0, dp(6), 0, 0)

        card.addView(title)
        card.addView(subtitle)

        card.setOnClickListener {
            openRiskDashboard(car.year, car.make, car.model, car.category)
        }

        layoutAlternativeList.addView(card)
    }

    private fun loadWeatherAdvice(
        city: String,
        textWeatherMode: TextView,
        textWeatherAdvice: TextView,
        textWeatherColor: TextView
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            textWeatherMode.text = "Weather Lens: Loading"
            textWeatherAdvice.text = "Checking current weather..."
            textWeatherColor.text = ""

            val task: Deferred<WeatherInfo> = CoroutineScope(Dispatchers.IO).async {
                WeatherClient().getCurrentWeather(city)
            }

            try {
                val weatherInfo = task.await()

                val advice = WeatherAdvisor.advise(
                    weatherInfo,
                    city,
                    currentPriority,
                    currentCategory
                )

                textWeatherMode.text = advice.modeName

                if (weatherInfo.success) {
                    textWeatherAdvice.text =
                        weatherInfo.city +
                                " · " +
                                weatherInfo.description +
                                " · " +
                                String.format("%.1f", weatherInfo.tempC) +
                                "°C\n" +
                                advice.advice
                } else {
                    textWeatherAdvice.text = advice.advice
                }

                textWeatherColor.text = advice.colorSuggestion

            } catch (e: Exception) {
                textWeatherMode.text = "Weather Lens: Fallback"
                textWeatherAdvice.text = "Check: tires · brakes · wipers"
                textWeatherColor.text = "Ask: Any water leak, rust, or battery issue?"
            }
        }
    }

    private fun makeFinalVerdictWithWallet(
        riskScore: Int,
        budgetDefenseScore: Int,
        walletScore: Int,
        priority: String
    ): String {
        if (riskScore >= 80 && budgetDefenseScore >= 75 && walletScore >= 75) {
            return "✅ STRONG CANDIDATE\nThe data can defend this purchase. Still check service records before payment."
        }

        if (walletScore < 50 && priority == "Wallet Survivor") {
            return "⚠️ WALLET SURVIVOR WARNING\nOwnership cost may be painful. Compare cheaper alternatives before buying."
        }

        if (riskScore < 60) {
            return "🚨 HIGH-RISK CANDIDATE\nInspect records, verify recall completion, and compare alternatives before buying."
        }

        if (budgetDefenseScore < 60) {
            return "⚠️ BUDGET DEFENSE WARNING\nThis car needs stronger evidence. Check alternatives before deciding."
        }

        return "⚠️ MIXED SIGNAL\nThis vehicle is usable, but inspection records should be checked before deciding."
    }

    private fun applyVerdictStyle(
        textFinalVerdict: TextView,
        riskScore: Int
    ) {
        textFinalVerdict.textSize = 19f
        textFinalVerdict.typeface = Typeface.DEFAULT_BOLD
        textFinalVerdict.setPadding(dp(18), dp(18), dp(18), dp(18))
        textFinalVerdict.setLineSpacing(6f, 1.0f)

        if (riskScore >= 80) {
            textFinalVerdict.setBackgroundResource(R.drawable.verdict_good_bg)
            textFinalVerdict.setTextColor(getColor(R.color.white))
        } else if (riskScore >= 60) {
            textFinalVerdict.setBackgroundResource(R.drawable.verdict_caution_bg)
            textFinalVerdict.setTextColor(getColor(R.color.white))
        } else {
            textFinalVerdict.setBackgroundResource(R.drawable.verdict_high_bg)
            textFinalVerdict.setTextColor(getColor(R.color.white))
        }
    }

    private fun setScoreRow(
        textView: TextView,
        progressBar: ProgressBar,
        title: String,
        score: Int
    ) {
        val cleanScore =
            if (score < 0) {
                0
            } else if (score > 100) {
                100
            } else {
                score
            }

        val color = getScoreColor(cleanScore)

        textView.text =
            title + ": " + cleanScore.toString() +
                    " / 100 (" + getScoreLabel(cleanScore) + ")"

        textView.setTextColor(color)
        progressBar.progress = cleanScore
        progressBar.progressTintList = ColorStateList.valueOf(color)
        progressBar.progressBackgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#E5E7EB"))
    }

    private fun getScoreColor(score: Int): Int {
        if (score >= 80) return Color.parseColor("#0B5A42")
        if (score >= 60) return Color.parseColor("#B97816")
        return Color.parseColor("#DC2626")
    }

    private fun getScoreLabel(score: Int): String {
        if (score >= 80) return "Good"
        if (score >= 60) return "Caution"
        return "Weak"
    }

    private fun makeRecallIssues(recallItems: ArrayList<RecallItem>): ArrayList<Issue> {
        val result = ArrayList<Issue>()
        var limit = recallItems.size

        if (limit > 5) limit = 5

        for (i in 0 until limit) {
            val item = recallItems[i]
            val component = item.component ?: "Unknown component"
            val campaign = item.nhtsaCampaignNumber ?: "No campaign number"
            val summary = item.summary ?: item.consequence ?: item.remedy ?: "No summary"

            result.add(
                Issue(
                    "RECALL",
                    component + " / " + campaign,
                    summary
                )
            )
        }

        return result
    }

    private fun makeComplaintIssues(
        complaintItems: ArrayList<ComplaintItem>
    ): ArrayList<Issue> {
        val result = ArrayList<Issue>()
        var limit = complaintItems.size

        if (limit > 5) limit = 5

        for (i in 0 until limit) {
            val item = complaintItems[i]
            val component = item.components ?: "Unknown component"
            val odiNumber = item.odiNumber?.toString() ?: "No ODI number"
            val summary = item.summary ?: "No summary"

            result.add(
                Issue(
                    "COMPLAINT",
                    component + " / " + odiNumber,
                    summary
                )
            )
        }

        return result
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