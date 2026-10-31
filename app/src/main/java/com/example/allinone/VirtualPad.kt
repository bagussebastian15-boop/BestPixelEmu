package com.example.allinone

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

class VirtualPad(
    context: Context,
    private val showXY: Boolean,
    private val haptic: Boolean,
    private val onKey: (action: Int, keyCode: Int) -> Unit
) : View(context) {

    private class Btn(val key: Int, val label: String) {
        var cx = 0f; var cy = 0f; var r = 0f
    }

    private val faceBtns = listOf(
        Btn(KeyEvent.KEYCODE_BUTTON_A, "A"),
        Btn(KeyEvent.KEYCODE_BUTTON_B, "B"),
        Btn(KeyEvent.KEYCODE_BUTTON_X, "X"),
        Btn(KeyEvent.KEYCODE_BUTTON_Y, "Y")
    )
    private val otherBtns = listOf(
        Btn(KeyEvent.KEYCODE_BUTTON_L1, "L"),
        Btn(KeyEvent.KEYCODE_BUTTON_R1, "R"),
        Btn(KeyEvent.KEYCODE_BUTTON_SELECT, "SEL"),
        Btn(KeyEvent.KEYCODE_BUTTON_START, "START")
    )
    private val allBtns get() = (if (showXY) faceBtns else faceBtns.take(2)) + otherBtns

    private val dirs = listOf(
        Triple(KeyEvent.KEYCODE_DPAD_UP, 0, -1),
        Triple(KeyEvent.KEYCODE_DPAD_DOWN, 0, 1),
        Triple(KeyEvent.KEYCODE_DPAD_LEFT, -1, 0),
        Triple(KeyEvent.KEYCODE_DPAD_RIGHT, 1, 0)
    )

    private var dpadX = 0f; private var dpadY = 0f; private var dpadR = 0f
    private val pressed = mutableSetOf<Int>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val base = min(w, h).toFloat()
        val margin = base * 0.06f

        dpadR = base * 0.22f
        dpadX = margin + dpadR * 1.2f
        dpadY = h - margin - dpadR * 1.2f

        val cx = w - margin - base * 0.30f
        val cy = h - margin - base * 0.24f
        val d = base * 0.15f
        val r = base * 0.08f
        // Tata letak ala Nintendo: A kanan, B bawah, X atas, Y kiri
        faceBtns[0].apply { this.cx = cx + d; this.cy = cy; this.r = r }
        faceBtns[1].apply { this.cx = cx; this.cy = cy + d; this.r = r }
        faceBtns[2].apply { this.cx = cx; this.cy = cy - d; this.r = r }
        faceBtns[3].apply { this.cx = cx - d; this.cy = cy; this.r = r }

        otherBtns[0].apply { this.cx = margin + base * 0.14f; this.cy = margin + base * 0.08f; this.r = base * 0.08f }
        otherBtns[1].apply { this.cx = w - margin - base * 0.14f; this.cy = margin + base * 0.08f; this.r = base * 0.08f }
        otherBtns[2].apply { this.cx = w / 2f - base * 0.14f; this.cy = h - margin - base * 0.05f; this.r = base * 0.06f }
        otherBtns[3].apply { this.cx = w / 2f + base * 0.14f; this.cy = h - margin - base * 0.05f; this.r = base * 0.06f }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val now = mutableSetOf<Int>()
        val ending = e.actionMasked == MotionEvent.ACTION_UP || e.actionMasked == MotionEvent.ACTION_CANCEL
        if (!ending) {
            for (i in 0 until e.pointerCount) {
                if (e.actionMasked == MotionEvent.ACTION_POINTER_UP && i == e.actionIndex) continue
                hit(e.getX(i), e.getY(i), now)
            }
        }
        update(now)
        return true
    }

    private fun hit(x: Float, y: Float, out: MutableSet<Int>) {
        for (b in allBtns) {
            val rr = b.r * 1.3f
            val dx = x - b.cx; val dy = y - b.cy
            if (dx * dx + dy * dy <= rr * rr) out.add(b.key)
        }
        val dx = x - dpadX; val dy = y - dpadY
        val dist = hypot(dx, dy)
        if (dist > dpadR * 0.25f && dist < dpadR * 1.6f) {
            if (abs(dx) > dist * 0.4f) out.add(if (dx > 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT)
            if (abs(dy) > dist * 0.4f) out.add(if (dy > 0) KeyEvent.KEYCODE_DPAD_DOWN else KeyEvent.KEYCODE_DPAD_UP)
        }
    }

    private fun update(now: Set<Int>) {
        val down = now - pressed
        val up = pressed - now
        if (down.isEmpty() && up.isEmpty()) return
        for (k in down) {
            onKey(KeyEvent.ACTION_DOWN, k)
            if (haptic) performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        for (k in up) onKey(KeyEvent.ACTION_UP, k)
        pressed.clear(); pressed.addAll(now)
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        // D-pad
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE; paint.alpha = 35
        c.drawCircle(dpadX, dpadY, dpadR * 1.15f, paint)
        textPaint.textSize = dpadR * 0.35f
        for ((key, dx, dy) in dirs) {
            val px = dpadX + dx * dpadR * 0.65f
            val py = dpadY + dy * dpadR * 0.65f
            paint.color = Color.WHITE; paint.alpha = if (key in pressed) 150 else 70
            c.drawCircle(px, py, dpadR * 0.3f, paint)
            textPaint.alpha = 220
            val arrow = when (key) {
                KeyEvent.KEYCODE_DPAD_UP -> "▲"
                KeyEvent.KEYCODE_DPAD_DOWN -> "▼"
                KeyEvent.KEYCODE_DPAD_LEFT -> "◀"
                else -> "▶"
            }
            c.drawText(arrow, px, py + textPaint.textSize * 0.35f, textPaint)
        }
        // Tombol lain
        for (b in allBtns) {
            paint.color = Color.WHITE; paint.alpha = if (b.key in pressed) 150 else 70
            c.drawCircle(b.cx, b.cy, b.r, paint)
            textPaint.textSize = if (b.label.length > 1) b.r * 0.5f else b.r * 0.85f
            textPaint.alpha = 230
            c.drawText(b.label, b.cx, b.cy + textPaint.textSize * 0.35f, textPaint)
        }
    }
}
