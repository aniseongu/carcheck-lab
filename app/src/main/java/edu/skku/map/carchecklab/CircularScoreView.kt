package edu.skku.map.carchecklab

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircularScoreView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var score: Int = 0
    private var title: String = "Risk"
    private var label: String = "Score"
    private var scoreColor: Int = Color.parseColor("#0B5A42")

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scorePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        backgroundPaint.color = Color.parseColor("#E2E8E5")
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = 20f
        backgroundPaint.strokeCap = Paint.Cap.ROUND

        arcPaint.style = Paint.Style.STROKE
        arcPaint.strokeWidth = 20f
        arcPaint.strokeCap = Paint.Cap.ROUND

        scorePaint.color = Color.parseColor("#111827")
        scorePaint.textAlign = Paint.Align.CENTER
        scorePaint.isFakeBoldText = true
        scorePaint.typeface = FontHelper.dashboardExtraBold(context)

        titlePaint.color = Color.parseColor("#374151")
        titlePaint.textAlign = Paint.Align.CENTER
        titlePaint.isFakeBoldText = true
        titlePaint.typeface = FontHelper.dashboardSemiBold(context)

        labelPaint.color = Color.parseColor("#6B7280")
        labelPaint.textAlign = Paint.Align.CENTER
        labelPaint.isFakeBoldText = true
        labelPaint.typeface = FontHelper.bodyMedium(context)
    }

    fun setScoreValue(
        newScore: Int,
        newTitle: String,
        newLabel: String,
        newColor: Int
    ) {
        score =
            if (newScore < 0) {
                0
            } else if (newScore > 100) {
                100
            } else {
                newScore
            }

        title = newTitle
        label = newLabel
        scoreColor = newColor
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthValue = width.toFloat()
        val heightValue = height.toFloat()
        val size = minOf(widthValue, heightValue)

        val padding = size * 0.12f
        val rect = RectF(
            padding,
            padding,
            size - padding,
            size - padding
        )

        val centerX = widthValue / 2f
        val centerY = heightValue / 2f

        scorePaint.textSize = size * 0.26f
        titlePaint.textSize = size * 0.085f
        labelPaint.textSize = size * 0.07f

        canvas.drawArc(rect, 135f, 270f, false, backgroundPaint)

        arcPaint.color = scoreColor
        val sweepAngle = 270f * score.toFloat() / 100f
        canvas.drawArc(rect, 135f, sweepAngle, false, arcPaint)

        canvas.drawText(score.toString(), centerX, centerY - size * 0.02f, scorePaint)
        canvas.drawText(title, centerX, centerY + size * 0.16f, titlePaint)
        canvas.drawText(label, centerX, centerY + size * 0.27f, labelPaint)
    }
}