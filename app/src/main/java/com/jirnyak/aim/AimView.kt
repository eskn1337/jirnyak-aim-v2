package com.jirnyak.aim

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class AimView(c: Context) : View(c) {
    private val aimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF00FF00"); strokeWidth = 2f; style = Paint.Style.STROKE
    }
    private val cuePaint = Paint(aimPaint).apply { color = Color.parseColor("#FFFFFF00"); pathEffect = DashPathEffect(floatArrayOf(18f, 12f), 0f) }
    private val tgtPaint = Paint(aimPaint).apply { color = Color.parseColor("#FFFF3355"); pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) }
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = aimPaint.color; style = Paint.Style.FILL }

    private var tx = 0f; private var ty = 0f
    private var target: Vec? = null

    init {
        setOnTouchListener { _, e ->
            if (e.action == MotionEvent.ACTION_MOVE || e.action == MotionEvent.ACTION_DOWN) {
                tx = e.x; ty = e.y; invalidate()
            }
            true
        }
    }

    private fun pathOf(pts: List<Vec>, paint: Paint, canvas: Canvas) {
        if (pts.size < 2) return
        val p = Path().apply { moveTo(pts[0].x, pts[0].y); for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y) }
        canvas.drawPath(p, paint)
    }

    override fun onDraw(canvas: Canvas) {
        if (!Config.aimOn) return
        val physics = Physics(Config.tableW, Config.tableH, Config.ballR, Config.bounces)
        val cx = width / 2f; val cy = height / 2f
        val aimDir = Vec(cx - tx, cy - ty).norm()
        val res = physics.predict(Vec(cx, cy), aimDir, target)
        pathOf(res.aim, aimPaint, canvas)
        if (Config.cueOn) pathOf(res.cue, cuePaint, canvas)
        if (Config.targetOn) res.target?.let { pathOf(it, tgtPaint, canvas) }
        val end = res.aim.lastOrNull() ?: return
        canvas.drawCircle(end.x, end.y, aimPaint.strokeWidth * 3, dot)
    }
}
