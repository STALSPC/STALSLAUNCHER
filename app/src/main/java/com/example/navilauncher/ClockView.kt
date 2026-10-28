package com.example.navilauncher

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

/** 아날로그 시계: 청록 링 + 흰색 시침/분침 + 청록 초침 (1초마다 갱신) */
class ClockView @JvmOverloads constructor(ctx: Context, attrs: AttributeSet? = null) : View(ctx, attrs) {

    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val hand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; strokeCap = Paint.Cap.ROUND
    }
    private val secHand = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#35C6C0"); strokeCap = Paint.Cap.ROUND
    }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.parseColor("#35C6C0")
    }

    private var running = false

    private val ticker = object : Runnable {
        override fun run() {
            if (!running) return
            invalidate()
            // 다음 '정각 초'에 맞춰 갱신
            postDelayed(this, 1000 - (System.currentTimeMillis() % 1000))
        }
    }

    private fun start() {
        if (running) return
        running = true
        removeCallbacks(ticker)
        post(ticker)
    }

    private fun stop() {
        running = false
        removeCallbacks(ticker)
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); start() }
    override fun onDetachedFromWindow() { stop(); super.onDetachedFromWindow() }
    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE) start() else stop()
    }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat(); val h = height.toFloat()
        val cx = w / 2; val cy = h / 2
        val r = minOf(w, h) / 2 - 6f
        if (r <= 0f) return
        val sw = r * 0.09f

        ring.strokeWidth = sw
        ring.shader = LinearGradient(cx, cy - r, cx, cy + r,
            Color.parseColor("#2BAA9C"), Color.parseColor("#1F6E9A"), Shader.TileMode.CLAMP)
        c.drawCircle(cx, cy, r - sw / 2, ring)

        val cal = Calendar.getInstance()
        val sec = cal.get(Calendar.SECOND)
        val min = cal.get(Calendar.MINUTE) + sec / 60f
        val hr = (cal.get(Calendar.HOUR) % 12) + min / 60f

        hand.strokeWidth = r * 0.075f
        drawHand(c, cx, cy, hr * 30f, r * 0.30f, r * 0.62f, hand)
        hand.strokeWidth = r * 0.055f
        drawHand(c, cx, cy, min * 6f, 0f, r * 0.80f, hand)
        secHand.strokeWidth = r * 0.025f
        drawHand(c, cx, cy, sec * 6f, -r * 0.15f, r * 0.82f, secHand)

        dot.strokeWidth = r * 0.03f
        c.drawCircle(cx, cy, r * 0.055f, dot)
    }

    private fun drawHand(c: Canvas, cx: Float, cy: Float, deg: Float, from: Float, to: Float, p: Paint) {
        val a = Math.toRadians((deg - 90).toDouble())
        val dx = Math.cos(a).toFloat(); val dy = Math.sin(a).toFloat()
        c.drawLine(cx + dx * from, cy + dy * from, cx + dx * to, cy + dy * to, p)
    }
}
