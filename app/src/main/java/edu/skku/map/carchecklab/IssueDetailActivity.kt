package edu.skku.map.carchecklab

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class IssueDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_detail)
        FontHelper.applyToViewTree(this, window.decorView)

        val layoutIssueContainer: LinearLayout = findViewById(R.id.layoutIssueContainer)
        val textIssueSummary: TextView = findViewById(R.id.textIssueSummary)
        val buttonBackIssue: Button = findViewById(R.id.buttonBackIssue)

        val issues: ArrayList<Issue> = RiskDashboardActivity.issueList

        textIssueSummary.text =
            "Issue records loaded: " + issues.size.toString() +
                    "\nThis screen shows recall and complaint samples used as evidence."

        layoutIssueContainer.removeAllViews()

        if (issues.size == 0) {
            addIssueCard(
                layoutIssueContainer,
                Issue(
                    "EMPTY",
                    "No issue found",
                    "No recall or complaint sample was loaded."
                )
            )
        } else {
            for (i in 0 until issues.size) {
                addIssueCard(layoutIssueContainer, issues[i])
            }
        }

        buttonBackIssue.setOnClickListener {
            finish()
        }
    }

    private fun addIssueCard(container: LinearLayout, issue: Issue) {
        val typeTextView = TextView(this)
        typeTextView.text = issue.type
        typeTextView.textSize = 13f
        typeTextView.setTextColor(getTypeColor(issue.type))
        typeTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        typeTextView.setPadding(16, 16, 16, 4)

        val titleTextView = TextView(this)
        titleTextView.text = issue.title
        titleTextView.textSize = 17f
        titleTextView.setTextColor(Color.parseColor("#111827"))
        titleTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        titleTextView.setPadding(16, 4, 16, 4)

        val detailTextView = TextView(this)
        detailTextView.text = issue.detail
        detailTextView.textSize = 14f
        detailTextView.setTextColor(Color.parseColor("#374151"))
        detailTextView.setPadding(16, 4, 16, 16)
        detailTextView.setLineSpacing(4f, 1.0f)

        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setBackgroundResource(R.drawable.card_bg)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, 0, 0, 14)
        card.layoutParams = params

        card.addView(typeTextView)
        card.addView(titleTextView)
        card.addView(detailTextView)

        container.addView(card)
    }

    private fun getTypeColor(type: String): Int {
        if (type == "RECALL") {
            return Color.parseColor("#DC2626")
        }

        if (type == "COMPLAINT") {
            return Color.parseColor("#D97706")
        }

        return Color.parseColor("#2563EB")
    }
}