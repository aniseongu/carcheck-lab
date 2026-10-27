package edu.skku.map.carchecklab

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

data class DemoVehicle(
    var year: Int,
    var make: String,
    var model: String,
    var category: String,
    var label: String,
    var bgDrawable: Int
)

class VehicleSelectActivity : AppCompatActivity() {

    companion object {
        const val EXT_YEAR: String = "year"
        const val EXT_MAKE: String = "make"
        const val EXT_MODEL: String = "model"
        const val EXT_CATEGORY: String = "category"
    }

    private lateinit var textVehicleUserInfo: TextView
    private lateinit var layoutDemoVehicleList: LinearLayout

    private val demoVehicles: ArrayList<DemoVehicle> = arrayListOf(
        DemoVehicle(2022, "TESLA", "MODEL 3", "EV", "Electric Sedan", R.drawable.demo_car_ev),
        DemoVehicle(2022, "KIA", "EV6", "EV", "Electric Crossover", R.drawable.demo_car_ev),
        DemoVehicle(2022, "HYUNDAI", "IONIQ 5", "EV", "Electric Crossover", R.drawable.demo_car_ev),
        DemoVehicle(2024, "TESLA", "CYBERTRUCK", "EV TRUCK", "Electric Truck", R.drawable.demo_car_ev),

        DemoVehicle(2022, "TOYOTA", "CAMRY", "SEDAN", "Sedan", R.drawable.demo_car_sedan),
        DemoVehicle(2022, "HONDA", "ACCORD", "SEDAN", "Sedan", R.drawable.demo_car_sedan),
        DemoVehicle(2022, "HYUNDAI", "SONATA", "SEDAN", "Sedan", R.drawable.demo_car_sedan),
        DemoVehicle(2022, "GENESIS", "G80", "PREMIUM SEDAN", "Premium Sedan", R.drawable.demo_car_sedan),

        DemoVehicle(2022, "TOYOTA", "RAV4", "SUV", "SUV", R.drawable.demo_car_suv),
        DemoVehicle(2022, "HYUNDAI", "TUCSON", "SUV", "SUV", R.drawable.demo_car_suv),
        DemoVehicle(2022, "KIA", "SORENTO", "SUV", "SUV", R.drawable.demo_car_suv),
        DemoVehicle(2022, "GENESIS", "GV70", "PREMIUM SUV", "Premium SUV", R.drawable.demo_car_suv),

        DemoVehicle(2022, "KIA", "CARNIVAL", "MINIVAN", "Van", R.drawable.demo_car_van),
        DemoVehicle(2022, "TOYOTA", "SIENNA", "HYBRID MINIVAN", "Hybrid Van", R.drawable.demo_car_van),
        DemoVehicle(2022, "HONDA", "ODYSSEY", "MINIVAN", "Van", R.drawable.demo_car_van),

        DemoVehicle(2022, "FORD", "F-150", "TRUCK", "Truck", R.drawable.demo_car_truck),
        DemoVehicle(2022, "CHEVROLET", "SILVERADO 1500", "TRUCK", "Truck", R.drawable.demo_car_truck),
        DemoVehicle(2022, "FORD", "F-150 LIGHTNING", "EV TRUCK", "EV Truck", R.drawable.demo_car_truck)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vehicle_select)
        FontHelper.applyToViewTree(this, window.decorView)

        FontHelper.applyToViewTree(this, window.decorView)

        MarketData.init(this)

        textVehicleUserInfo = findViewById(R.id.textVehicleUserInfo)
        layoutDemoVehicleList = findViewById(R.id.layoutDemoVehicleList)

        val buttonEditProfile: Button = findViewById(R.id.buttonEditProfile)
        val buttonSearchByName: Button = findViewById(R.id.buttonSearchByName)
        val buttonDecodeVin: Button = findViewById(R.id.buttonDecodeVin)
        val buttonSavedReports: Button = findViewById(R.id.buttonSavedReports)
        val buttonLogout: Button = findViewById(R.id.buttonLogout)

        buttonEditProfile.backgroundTintList = null
        buttonSearchByName.backgroundTintList = null
        buttonDecodeVin.backgroundTintList = null
        buttonSavedReports.backgroundTintList = null
        buttonLogout.backgroundTintList = null

        buttonEditProfile.setOnClickListener {
            val intent = Intent(this, ProfileSetupActivity::class.java)
            intent.putExtra(ProfileSetupActivity.EXTRA_EDIT_MODE, true)
            intent.putExtra(
                ProfileSetupActivity.EXTRA_USER_ID,
                ServerSessionManager.getCurrentUserId(this)
            )
            startActivity(intent)
        }

        buttonSearchByName.setOnClickListener {
            val intent = Intent(this, CarSearchActivity::class.java)
            startActivity(intent)
        }

        buttonDecodeVin.setOnClickListener {
            val intent = Intent(this, VinDecodeActivity::class.java)
            startActivity(intent)
        }

        buttonSavedReports.setOnClickListener {
            val intent = Intent(this, SavedCarsActivity::class.java)
            startActivity(intent)
        }

        buttonLogout.setOnClickListener {
            ServerSessionManager.clearSession(this)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        renderDemoVehicleList()
    }

    override fun onResume() {
        super.onResume()
        updateProfileSummary()
    }

    private fun updateProfileSummary() {
        val currentUserId = ServerSessionManager.getCurrentUserId(this)
        val city = UserProfileManager.getCity(this)
        val priority = UserProfileManager.getPriority(this)

        textVehicleUserInfo.text =
            "User: " + if (currentUserId.isEmpty()) "Guest" else currentUserId +
                    "\nRegion: " + city +
                    "\nPriority: " + priority
    }

    private fun renderDemoVehicleList() {
        layoutDemoVehicleList.removeAllViews()

        for (i in 0 until demoVehicles.size) {
            val vehicle = demoVehicles[i]

            val row = LinearLayout(this)
            row.orientation = LinearLayout.VERTICAL
            row.setBackgroundResource(vehicle.bgDrawable)
            row.setPadding(dp(16), dp(14), dp(16), dp(14))

            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            rowParams.setMargins(0, 0, 0, dp(12))
            row.layoutParams = rowParams

            val topRow = LinearLayout(this)
            topRow.orientation = LinearLayout.HORIZONTAL
            topRow.gravity = Gravity.CENTER_VERTICAL

            val iconView = ImageView(this)
            iconView.setImageResource(getCategoryIcon(vehicle.category))
            iconView.scaleType = ImageView.ScaleType.FIT_CENTER
            iconView.contentDescription = vehicle.category

            val iconParams = LinearLayout.LayoutParams(
                dp(72),
                dp(72)
            )

            iconView.layoutParams = iconParams

            val titleBox = LinearLayout(this)
            titleBox.orientation = LinearLayout.VERTICAL
            titleBox.setPadding(dp(14), 0, 0, 0)

            val title = TextView(this)
            title.text =
                vehicle.year.toString() + " " +
                        vehicle.make + " " +
                        vehicle.model

            title.textSize = 19f
            title.typeface = FontHelper.dashboardBold(this)
            title.setTextColor(getColor(R.color.skku_green_950))

            val subtitle = TextView(this)
            subtitle.text = vehicle.label + " / " + vehicle.category
            subtitle.textSize = 14f
            subtitle.typeface = FontHelper.bodyMedium(this)
            subtitle.setTextColor(getColor(R.color.text_secondary))
            subtitle.setPadding(0, dp(4), 0, 0)

            titleBox.addView(title)
            titleBox.addView(subtitle)

            val titleBoxParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

            titleBox.layoutParams = titleBoxParams

            topRow.addView(iconView)
            topRow.addView(titleBox)

            val buttonRow = LinearLayout(this)
            buttonRow.orientation = LinearLayout.HORIZONTAL
            buttonRow.gravity = Gravity.CENTER_VERTICAL
            buttonRow.setPadding(0, dp(12), 0, 0)

            val reportButton = Button(this)
            reportButton.text = "Report"
            reportButton.isAllCaps = false
            reportButton.backgroundTintList = null
            reportButton.setBackgroundResource(R.drawable.button_skk_green)
            reportButton.setTextColor(getColor(R.color.white))
            reportButton.typeface = FontHelper.bodySemiBold(this)

            val reviewButton = Button(this)
            reviewButton.text = "Comments ★"
            reviewButton.isAllCaps = false
            reviewButton.backgroundTintList = null
            reviewButton.setBackgroundResource(R.drawable.button_skk_gold)
            reviewButton.setTextColor(getColor(R.color.skku_green_950))
            reviewButton.typeface = FontHelper.bodySemiBold(this)

            val reportParams = LinearLayout.LayoutParams(
                0,
                dp(46),
                1f
            )

            reportParams.setMargins(0, 0, dp(6), 0)

            val reviewParams = LinearLayout.LayoutParams(
                0,
                dp(46),
                1f
            )

            reviewParams.setMargins(dp(6), 0, 0, 0)

            reportButton.layoutParams = reportParams
            reviewButton.layoutParams = reviewParams

            reportButton.setOnClickListener {
                openRiskDashboard(vehicle.year, vehicle.make, vehicle.model, vehicle.category)
            }

            reviewButton.setOnClickListener {
                openReview(vehicle.year, vehicle.make, vehicle.model, vehicle.category)
            }

            buttonRow.addView(reportButton)
            buttonRow.addView(reviewButton)

            row.addView(topRow)
            row.addView(buttonRow)

            layoutDemoVehicleList.addView(row)
        }
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
        intent.putExtra(EXT_YEAR, year)
        intent.putExtra(EXT_MAKE, make)
        intent.putExtra(EXT_MODEL, model)
        intent.putExtra(EXT_CATEGORY, category)
        startActivity(intent)
    }

    private fun openReview(
        year: Int,
        make: String,
        model: String,
        category: String
    ) {
        val intent = Intent(this, VehicleReviewActivity::class.java)
        intent.putExtra(EXT_YEAR, year)
        intent.putExtra(EXT_MAKE, make)
        intent.putExtra(EXT_MODEL, model)
        intent.putExtra(EXT_CATEGORY, category)
        startActivity(intent)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}