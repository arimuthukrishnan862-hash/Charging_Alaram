package com.muthukrishnann.chargeguard.ui.home

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.min

class CircularBatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Float = 0f
    private var isCharging: Boolean = false

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 32f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#1E293B")
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 32f
        strokeCap = Paint.Cap.ROUND
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 44f
        strokeCap = Paint.Cap.ROUND
        alpha = 40
    }

    private val percentagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 100f
        color = Color.WHITE
        isFakeBoldText = true
    }

    private val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 34f
        color = Color.parseColor("#94A3B8")
    }

    private val oval = RectF()
    private var animator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null
    private var pulseAlpha: Int = 40

    init {
        startPulseAnimation()
    }

    private fun startPulseAnimation() {
        pulseAnimator?.cancel()
        pulseAnimator = ValueAnimator.ofInt(20, 80).apply {
            duration = 1400
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                pulseAlpha = it.animatedValue as Int
                if (isCharging) invalidate()
            }
            start()
        }
    }

    fun setBatteryData(targetPercentage: Int, charging: Boolean) {
        val target = targetPercentage.coerceIn(0, 100).toFloat()
        isCharging = charging

        animator?.cancel()
        animator = ValueAnimator.ofFloat(progress, target).apply {
            duration = 850
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val stroke = 32f
        val padding = stroke / 2f + 16f
        val diameter = min(w, h) - (padding * 2)
        val left = (w - diameter) / 2f
        val top = (h - diameter) / 2f
        oval.set(left, top, left + diameter, top + diameter)

        // Gradient shader from cyan to emerald
        val shader = LinearGradient(
            oval.left, oval.top, oval.right, oval.bottom,
            intArrayOf(Color.parseColor("#00E5FF"), Color.parseColor("#00E676")),
            null,
            Shader.TileMode.CLAMP
        )
        progressPaint.shader = shader
        glowPaint.shader = shader
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val sweepAngle = (progress / 100f) * 360f

        // 1. Draw Background Track Ring
        canvas.drawArc(oval, -90f, 360f, false, trackPaint)

        // 2. Draw Glow Ring when Charging
        if (isCharging && progress > 0) {
            glowPaint.alpha = pulseAlpha
            canvas.drawArc(oval, -90f, sweepAngle, false, glowPaint)
        }

        // 3. Draw Progress Arc
        if (progress > 0) {
            canvas.drawArc(oval, -90f, sweepAngle, false, progressPaint)
        }

        // 4. Draw Center Percentage Text
        val centerX = oval.centerX()
        val centerY = oval.centerY()

        val percentText = "${progress.toInt()}%"
        val textY = centerY - ((percentagePaint.descent() + percentagePaint.ascent()) / 2) - 16f
        canvas.drawText(percentText, centerX, textY, percentagePaint)

        // 5. Draw Charging Status Label
        val label = if (isCharging) "⚡ CHARGING" else "🔋 BATTERY"
        statusPaint.color = if (isCharging) Color.parseColor("#00E676") else Color.parseColor("#94A3B8")
        val labelY = textY + 68f
        canvas.drawText(label, centerX, labelY, statusPaint)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        pulseAnimator?.cancel()
        super.onDetachedFromWindow()
    }
}
