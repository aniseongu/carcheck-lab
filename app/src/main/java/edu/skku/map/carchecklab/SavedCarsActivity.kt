package edu.skku.map.carchecklab

import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SavedCarsActivity : AppCompatActivity() {

    companion object {
        var savedCars: ArrayList<SavedCarResult> = ArrayList<SavedCarResult>()
    }

    private lateinit var textSavedDescription: TextView
    private lateinit var listSavedCars: ListView
    private lateinit var adapter: SavedCarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_cars)
        FontHelper.applyToViewTree(this, window.decorView)

        textSavedDescription = findViewById<TextView>(R.id.textSavedDescription)
        listSavedCars = findViewById<ListView>(R.id.listSavedCars)

        val buttonClearSaved: Button = findViewById<Button>(R.id.buttonClearSaved)
        val buttonBackFromSaved: Button = findViewById<Button>(R.id.buttonBackFromSaved)

        adapter = SavedCarAdapter(this, savedCars)
        listSavedCars.adapter = adapter

        updateDescription()

        listSavedCars.setOnItemClickListener { parent, view, position, id ->
            val result: SavedCarResult = savedCars[position]

            Toast.makeText(
                this,
                result.vehicleName + " / Score " + result.score.toString(),
                Toast.LENGTH_SHORT
            ).show()
        }

        buttonClearSaved.setOnClickListener {
            savedCars.clear()
            adapter.notifyDataSetChanged()
            updateDescription()
            Toast.makeText(this, "Saved reports cleared.", Toast.LENGTH_SHORT).show()
        }

        buttonBackFromSaved.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
            updateDescription()
        }
    }

    private fun updateDescription() {
        if (savedCars.size == 0) {
            textSavedDescription.text =
                "No saved report yet. Analyze a vehicle and press SAVE THIS REPORT."
        } else {
            textSavedDescription.text =
                "Saved reports: " + savedCars.size.toString() +
                        "\nThis screen keeps analyzed vehicle reports for quick review during the demo."
        }
    }
}