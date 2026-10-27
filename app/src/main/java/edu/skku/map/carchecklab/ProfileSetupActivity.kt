package edu.skku.map.carchecklab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class ProfileSetupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EDIT_MODE: String = "extra_edit_mode"
        const val EXTRA_USER_ID: String = "extra_user_id"
    }

    private lateinit var textProfileTitle: TextView
    private lateinit var textProfileUser: TextView
    private lateinit var editCity: EditText
    private lateinit var spinnerPriority: Spinner
    private lateinit var buttonSaveProfile: Button
    private lateinit var buttonBackProfile: Button
    private lateinit var progressProfile: ProgressBar

    private var userId: String = ""
    private var isEditMode: Boolean = false

    private val priorityList = listOf(
        "Wallet Survivor",
        "Safety Nerd",
        "Hachagam Hunter",
        "Weather Safety First",
        "Tech Bro",
        "Family Commander"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)
        FontHelper.applyToViewTree(this, window.decorView)

        FontHelper.applyToViewTree(this, window.decorView)

        textProfileTitle = findViewById(R.id.textProfileTitle)
        textProfileUser = findViewById(R.id.textProfileUser)
        editCity = findViewById(R.id.editCity)
        spinnerPriority = findViewById(R.id.spinnerPriority)
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile)
        buttonBackProfile = findViewById(R.id.buttonBackProfile)
        progressProfile = findViewById(R.id.progressProfile)

        buttonSaveProfile.backgroundTintList = null
        buttonBackProfile.backgroundTintList = null

        isEditMode = intent.getBooleanExtra(EXTRA_EDIT_MODE, false)

        userId =
            intent.getStringExtra(EXTRA_USER_ID)
                ?: intent.getStringExtra("userId")
                        ?: ServerSessionManager.getCurrentUserId(this)

        if (userId.isEmpty()) {
            userId = ServerSessionManager.getCurrentUserId(this)
        }

        setupSpinner()
        setupInitialValues()
        setupButtons()
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            priorityList
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = adapter
    }

    private fun setupInitialValues() {
        textProfileUser.text =
            "User ID: " + if (userId.isEmpty()) "Not found" else userId

        if (isEditMode) {
            textProfileTitle.text = "Edit your buyer profile"
            buttonSaveProfile.text = "Save changes"
            buttonBackProfile.text = "Back to vehicle list"
        } else {
            textProfileTitle.text = "Set your buyer profile"
            buttonSaveProfile.text = "Save profile"
            buttonBackProfile.text = "Back to login"
        }

        editCity.setText(ServerSessionManager.getCity(this))

        val savedPriority = ServerSessionManager.getPriority(this)
        val priorityIndex = findPriorityIndex(savedPriority)

        if (priorityIndex >= 0) {
            spinnerPriority.setSelection(priorityIndex)
        }
    }

    private fun setupButtons() {
        buttonSaveProfile.setOnClickListener {
            saveProfile()
        }

        buttonBackProfile.setOnClickListener {
            if (isEditMode) {
                finish()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun findPriorityIndex(priority: String): Int {
        for (i in priorityList.indices) {
            if (priorityList[i] == priority) {
                return i
            }
        }

        return 0
    }

    private fun saveProfile() {
        val city = editCity.text.toString().trim()
        val priority = spinnerPriority.selectedItem.toString()

        if (userId.isEmpty()) {
            Toast.makeText(
                this,
                "User ID was not found. Please log in again.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (city.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter your city.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        setLoading(true)

        thread {
            val client = ServerAuthClient()
            val result = client.updateProfile(userId, city, priority)

            runOnUiThread {
                setLoading(false)
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()

                if (result.success) {
                    ServerSessionManager.saveSession(
                        this,
                        result.userId,
                        result.city,
                        result.priority,
                        result.hasProfile
                    )

                    UserProfileManager.saveProfile(
                        this,
                        result.city,
                        result.priority
                    )

                    openVehicleSelect()
                }
            }
        }
    }

    private fun openVehicleSelect() {
        val intent = Intent(this, VehicleSelectActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        progressProfile.visibility =
            if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        buttonSaveProfile.isEnabled = !isLoading
        buttonBackProfile.isEnabled = !isLoading
    }
}