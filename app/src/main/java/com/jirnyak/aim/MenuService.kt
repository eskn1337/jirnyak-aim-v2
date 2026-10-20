package com.jirnyak.aim

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import android.widget.*

class MenuService : Service() {
    private lateinit var wm: WindowManager
    private lateinit var root: LinearLayout
    private lateinit var panel: LinearLayout
    private lateinit var panelLp: WindowManager.LayoutParams
    private var expanded = false

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#CC101010")) }

        val header = TextView(this).apply {
            text = "≡ jirnyak.aim"
            setTextColor(Color.WHITE); textSize = 14f
            setPadding(24, 16, 24, 16)
            setOnClickListener { toggle() }
        }
        root.addView(header)

        panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE }
        panel.addView(switch("Линия прицела", Config.aimOn) { Config.aimOn = it })
        panel.addView(switch("Предикт битка", Config.cueOn) { Config.cueOn = it })
        panel.addView(switch("Предикт цели", Config.targetOn) { Config.targetOn = it })
        panel.addView(stepper("Отскоки", Config.bounces, 0, 5) { Config.bounces = it })
        root.addView(panel)

        panelLp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP or Gravity.START; x = 40; y = 200 }

        wm.addView(root, panelLp)

        header.setOnTouchListener(object : View.OnTouchListener {
            var sx = 0; var sy = 0; var ox = 0; var oy = 0
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { sx = e.rawX.toInt(); sy = e.rawY.toInt(); ox = panelLp.x; oy = panelLp.y }
                    MotionEvent.ACTION_MOVE -> { panelLp.x = ox + (e.rawX.toInt() - sx); panelLp.y = oy + (e.rawY.toInt() - sy); wm.updateViewLayout(root, panelLp) }
                }
                return false
            }
        })
    }

    private fun toggle() {
        expanded = !expanded
        panel.visibility = if (expanded) View.VISIBLE else View.GONE
        wm.updateViewLayout(root, panelLp)
    }

    private fun switch(label: String, init: Boolean, onChange: (Boolean) -> Unit): View =
        Switch(this).apply { text = label; isChecked = init; setOnCheckedChangeListener { _, v -> onChange(v) } }

    private fun stepper(label: String, init: Int, min: Int, max: Int, onChange: (Int) -> Unit): View {
        var v = init
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(24, 8, 24, 8) }
        val tv = TextView(this).apply { text = "$label: $v"; setTextColor(Color.WHITE); textSize = 14f }
        fun upd() { tv.text = "$label: $v"; onChange(v) }
        row.addView(TextView(this).apply { text = "−"; setTextColor(Color.WHITE); textSize = 22f; setPadding(16,0,16,0); setOnClickListener { if (v > min) { v--; upd() } } })
        row.addView(tv)
        row.addView(TextView(this).apply { text = "+"; setTextColor(Color.WHITE); textSize = 22f; setPadding(16,0,16,0); setOnClickListener { if (v < max) { v++; upd() } } })
        return row
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::root.isInitialized) wm.removeView(root)
    }
}
