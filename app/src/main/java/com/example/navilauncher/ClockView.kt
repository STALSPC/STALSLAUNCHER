package com.example.navilauncher

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

/** 원본 화면의 아날로그 시계 (청록 링 + 흰색 둥근 시침/분침) */
class ClockView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : View(ctx, attrs) {

    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val hand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; strokeCap = Paint.Cap.ROUND
    }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.parseColor("#35C6C0")
    }
    private val txt = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#6FB7C4"); textAlign = Paint.Align.LEFT
    }

    private val tick = object : Runnable {
        override fun run() { invalidate(); postDelayed(this, 1000) }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); post(tick) }
    override fun onDetachedFromWindow() { removeCallbacks(tick); super.onDetachedFromWindow() }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val cx = w / 2; val cy = h / 2
        val r = minOf(w, h) / 2 - 6f
        val sw = r * 0.09f

        ring.strokeWidth = sw
        ring.shader = LinearGradient(cx, cy - r, cx, cy + r,
            Color.parseColor("#2BAA9C"), Color.parseColor("#1F6E9A"), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r - sw / 2, ring)

        val cal = Calendar.getInstance()
        val m = cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) / 60f
        val hr = (cal.get(Calendar.HOUR) % 12) + m / 60f

        hand.strokeWidth = r * 0.075f
        drawHand(c, cx, cy, hr * 30f, r * 0.30f, r * 0.62f)
        hand.strokeWidth = r * 0.055f
        drawHand(c, cx, cy, m * 6f, 0f, r * 0.80f)

        dot.strokeWidth = r * 0.03f
        c.drawCircle(cx, cy, r * 0.055f, dot)
        txt.textSize = r * 0.13f
        val ampm = if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
        c.drawText("%02d %s".format(cal.get(Calendar.MINUTE), ampm), cx + r * 0.12f, cy + r * 0.05f, txt)
    }

    private fun drawHand(c: Canvas, cx: Float, cy: Float, deg: Float, from: Float, to: Float) {
        val a = Math.toRadians((deg - 90).toDouble())
        val dx = Math.cos(a).toFloat(); val dy = Math.sin(a).toFloat()
        c.drawLine(cx + dx * from, cy + dy * from, cx + dx * to, cy + dy * to, hand)
    }
}
