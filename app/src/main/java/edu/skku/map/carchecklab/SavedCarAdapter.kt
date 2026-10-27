package edu.skku.map.carchecklab

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class SavedCarAdapter(
    private val context: Context,
    private val savedCars: ArrayList<SavedCarResult>
) : BaseAdapter() {

    override fun getCount(): Int {
        return savedCars.size
    }

    override fun getItem(position: Int): Any {
        return savedCars[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View =
            convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_saved_car, parent, false)

        val textSavedVehicleName: TextView =
            view.findViewById<TextView>(R.id.textSavedVehicleName)

        val textSavedScoreGrade: TextView =
            view.findViewById<TextView>(R.id.textSavedScoreGrade)

        val textSavedStats: TextView =
            view.findViewById<TextView>(R.id.textSavedStats)

        val textSavedTapHint: TextView =
            view.findViewById<TextView>(R.id.textSavedTapHint)

        val result: SavedCarResult = savedCars[position]

        textSavedVehicleName.text = result.vehicleName

        textSavedScoreGrade.text =
            "Score: " + result.score.toString() + " / " + result.grade

        textSavedScoreGrade.setTextColor(getGradeColor(result.grade))

        textSavedStats.text =
            "Recall: " + result.recallCount.toString() +
                    " / Complaint: " + result.complaintCount.toString() +
                    " / Safety: " + result.safetyRating

        textSavedTapHint.text =
            "Tap to preview report summary. Saved Reports is kept as a simple demo review screen."

        return view
    }

    private fun getGradeColor(grade: String): Int {
        if (grade.contains("LOW")) {
            return Color.parseColor("#16A34A")
        }

        if (grade.contains("MEDIUM")) {
            return Color.parseColor("#D97706")
        }

        if (grade.contains("HIGH")) {
            return Color.parseColor("#DC2626")
        }

        return Color.parseColor("#111827")
    }
}