package com.example.allinone

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.ShaderConfig

class GameActivity : AppCompatActivity() {

    private lateinit var retroView: GLRetroView
    private lateinit var pad: VirtualPad
    private var lastRatio = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= 28) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        val uri = android.net.Uri.parse(intent.getStringExtra("uri"))
        val core = intent.getStringExtra("core")!!
        val bytes = contentResolver.openInputStream(uri)!!.use { it.readBytes() }

        val data = GLRetroViewData(this).apply {
            coreFilePath = core
            gameFileBytes = bytes
            systemDirectory = filesDir.absolutePath
            savesDirectory = getExternalFilesDir("saves")!!.absolutePath
            shader = ShaderConfig.Default
        }
        retroView = GLRetroView(this, data)
        lifecycle.addObserver(retroView)

        val root = FrameLayout(this)
        root.addView(retroView, FrameLayout.LayoutParams(-1, -1))

        val showXY = core != GameSystem.GBA.coreLib
        pad = VirtualPad(this, showXY, Settings.haptic(this)) { action, key ->
            retroView.sendKeyEvent(action, key)
        }
        pad.visibility = if (Settings.showPad(this)) View.VISIBLE else View.GONE
        root.addView(pad, FrameLayout.LayoutParams(-1, -1))

        // Tombol menu di tengah atas
        val menuBtn = Button(this).apply {
            text = "☰"
            alpha = 0.5f
            minWidth = 0
            minimumWidth = 0
            setOnClickListener { showMenu() }
        }
        root.addView(
            menuBtn,
            FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.CENTER_HORIZONTAL)
        )

        setContentView(root)
        applyFullscreen()

        retroView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> applyStretch() }

        // Tombol Back membuka menu, bukan langsung keluar
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = showMenu()
        })
    }

    private fun showMenu() {
        AlertDialog.Builder(this)
            .setTitle("Menu")
            .setItems(arrayOf("Lanjutkan", "Pengaturan", "Keluar ke library game")) { _, i ->
                when (i) {
                    1 -> Settings.showDialog(this) {
                        applyFullscreen()
                        lastRatio = -1f
                        applyStretch()
                        pad.visibility = if (Settings.showPad(this)) View.VISIBLE else View.GONE
                    }
                    2 -> finish()
                }
            }
            .show()
    }

    // Isi seluruh layar: aspect ratio gambar disamakan dengan ukuran layar
    private fun applyStretch() {
        val w = retroView.width
        val h = retroView.height
        if (w == 0 || h == 0) return
        val ratio = if (Settings.stretch(this)) w.toFloat() / h else 0f
        if (ratio == lastRatio) return
        lastRatio = ratio
        try {
            retroView.javaClass
                .getMethod("setViewportAspectRatio", java.lang.Float.TYPE)
                .invoke(retroView, ratio)
        } catch (t: Throwable) {
            Log.w("GameActivity", "Tidak bisa mengatur aspect ratio", t)
        }
    }

    private fun applyFullscreen() {
        val full = Settings.fullscreen(this)
        WindowCompat.setDecorFitsSystemWindows(window, !full)
        val ctrl = WindowInsetsControllerCompat(window, window.decorView)
        if (full) {
            ctrl.hide(WindowInsetsCompat.Type.systemBars())
            ctrl.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            ctrl.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) applyFullscreen()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.source and InputDevice.SOURCE_GAMEPAD != 0) {
            retroView.sendKeyEvent(event.action, event.keyCode)
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent): Boolean {
        retroView.sendMotionEvent(
            GLRetroView.MOTION_SOURCE_DPAD,
            event.getAxisValue(MotionEvent.AXIS_HAT_X),
            event.getAxisValue(MotionEvent.AXIS_HAT_Y),
            0
        )
        return true
    }
}
