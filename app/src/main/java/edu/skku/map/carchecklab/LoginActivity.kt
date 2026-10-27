package edu.skku.map.carchecklab

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    private lateinit var editUserId: EditText
    private lateinit var editPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button
    private lateinit var progressLogin: ProgressBar
    private lateinit var textLoginStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        try {
            FontHelper.applyToViewTree(this, window.decorView)
        } catch (e: Exception) {
            // Font fallback. Login should never fail because of font loading.
        }

        editUserId = findViewById(R.id.editUserId)
        editPassword = findViewById(R.id.editPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegister = findViewById(R.id.buttonRegister)
        progressLogin = findViewById(R.id.progressLogin)
        textLoginStatus = findViewById(R.id.textLoginStatus)

        buttonLogin.backgroundTintList = null
        buttonRegister.backgroundTintList = null

        clearLoginFields()
        hideStatusMessage()

        buttonLogin.setOnClickListener {
            submitAuth(false)
        }

        buttonRegister.setOnClickListener {
            submitAuth(true)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (::editUserId.isInitialized && ::editPassword.isInitialized) {
            clearLoginFields()
            hideStatusMessage()
        }
    }

    private fun clearLoginFields() {
        editUserId.setText("")
        editPassword.setText("")
        editUserId.clearFocus()
        editPassword.clearFocus()
    }

    private fun hideStatusMessage() {
        textLoginStatus.text = ""
        textLoginStatus.visibility = View.GONE
    }

    private fun showStatusMessage(message: String) {
        textLoginStatus.text = message
        textLoginStatus.visibility = View.VISIBLE
    }

    private fun submitAuth(isRegister: Boolean) {
        val userId = editUserId.text.toString().trim()
        val password = editPassword.text.toString().trim()

        if (userId.isEmpty() || password.isEmpty()) {
            showStatusMessage("Please enter both user ID and password.")
            Toast.makeText(
                this,
                "Please enter both user ID and password.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        setLoading(true)

        thread {
            val client = ServerAuthClient()

            val result =
                if (isRegister) {
                    client.register(userId, password)
                } else {
                    client.login(userId, password)
                }

            runOnUiThread {
                setLoading(false)
                showStatusMessage(result.message)
                Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()

                if (result.success) {
                    ServerSessionManager.saveSession(
                        this,
                        result.userId,
                        result.city,
                        result.priority,
                        result.hasProfile
                    )

                    if (result.hasProfile) {
                        openVehicleSelect()
                    } else {
                        openProfileSetup(result.userId)
                    }
                }
            }
        }
    }

    private fun openProfileSetup(userId: String) {
        val intent = Intent(this, ProfileSetupActivity::class.java)
        intent.putExtra(ProfileSetupActivity.EXTRA_USER_ID, userId)
        intent.putExtra(ProfileSetupActivity.EXTRA_EDIT_MODE, false)
        startActivity(intent)
    }

    private fun openVehicleSelect() {
        val intent = Intent(this, VehicleSelectActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setLoading(isLoading: Boolean) {
        progressLogin.visibility =
            if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

        buttonLogin.isEnabled = !isLoading
        buttonRegister.isEnabled = !isLoading
    }
}