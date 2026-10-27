package edu.skku.map.carchecklab

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class VinDecodeActivity : AppCompatActivity() {

    private lateinit var editVin: EditText
    private lateinit var textVinResult: TextView
    private var decodedYear: Int = 2022
    private var decodedMake: String = "KIA"
    private var decodedModel: String = "EV6"
    private var decodedCategory: String = "Vehicle"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vin_decode)
        FontHelper.applyToViewTree(this, window.decorView)

        editVin = findViewById(R.id.editVin)
        textVinResult = findViewById(R.id.textVinResult)

        val buttonDecodeVin: Button = findViewById(R.id.buttonDecodeVinNow)
        val buttonOpenDecoded: Button = findViewById(R.id.buttonOpenDecodedVehicle)
        val buttonBackVin: Button = findViewById(R.id.buttonBackVin)

        buttonOpenDecoded.isEnabled = false

        buttonDecodeVin.setOnClickListener {
            val vin: String = editVin.text.toString().trim()

            if (vin.length < 8) {
                Toast.makeText(this, "Enter a valid VIN.", Toast.LENGTH_SHORT).show()
            } else {
                decodeVin(vin, buttonOpenDecoded)
            }
        }

        buttonOpenDecoded.setOnClickListener {
            val intent = Intent(this, RiskDashboardActivity::class.java)
            intent.putExtra(VehicleSelectActivity.EXT_YEAR, decodedYear)
            intent.putExtra(VehicleSelectActivity.EXT_MAKE, decodedMake)
            intent.putExtra(VehicleSelectActivity.EXT_MODEL, decodedModel)
            intent.putExtra(VehicleSelectActivity.EXT_CATEGORY, decodedCategory)
            startActivity(intent)
        }

        buttonBackVin.setOnClickListener {
            finish()
        }
    }

    private fun decodeVin(
        vin: String,
        buttonOpenDecoded: Button
    ) {
        textVinResult.text = "Decoding VIN through NHTSA vPIC API..."

        CoroutineScope(Dispatchers.Main).launch {
            val task: Deferred<VinDecodeResponse> = CoroutineScope(Dispatchers.IO).async {
                NhtsaClient().decodeVin(vin)
            }

            try {
                val response: VinDecodeResponse = task.await()
                val items: ArrayList<VinDecodeItem> = response.results ?: ArrayList()

                val make: String = findVinValue(items, "Make")
                val model: String = findVinValue(items, "Model")
                val modelYear: String = findVinValue(items, "Model Year")
                val vehicleType: String = findVinValue(items, "Vehicle Type")

                decodedYear = modelYear.toIntOrNull() ?: 2022
                decodedMake = if (make.isEmpty()) "KIA" else make
                decodedModel = if (model.isEmpty()) "EV6" else model
                decodedCategory = if (vehicleType.isEmpty()) "Vehicle" else vehicleType

                textVinResult.text =
                    "VIN decoded successfully." +
                            "\nYear: " + decodedYear.toString() +
                            "\nMake: " + decodedMake +
                            "\nModel: " + decodedModel +
                            "\nType: " + decodedCategory

                buttonOpenDecoded.isEnabled = true

            } catch (e: Exception) {
                textVinResult.text =
                    "VIN decode failed. Check VIN or internet connection."
                buttonOpenDecoded.isEnabled = false
            }
        }
    }

    private fun findVinValue(
        items: ArrayList<VinDecodeItem>,
        variableName: String
    ): String {
        for (i in 0 until items.size) {
            val item: VinDecodeItem = items[i]
            val variable: String = item.variable ?: ""

            if (variable.equals(variableName, true)) {
                return item.value ?: ""
            }
        }

        return ""
    }
}