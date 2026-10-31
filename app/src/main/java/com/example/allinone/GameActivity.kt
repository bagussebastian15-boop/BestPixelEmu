package com.example.allinone

import android.os.Build
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData
import com.swordfish.libretrodroid.ShaderConfig

class GameActivity : AppCompatActivity() {

    private lateinit var retroView: GLRetroView

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

        if (Settings.showPad(this)) {
            val showXY = core != GameSystem.GBA.coreLib
            val pad = VirtualPad(this, showXY, Settings.haptic(this)) { action, key ->
                retroView.sendKeyEvent(action, key)
            }
            root.addView(pad, FrameLayout.LayoutParams(-1, -1))
        }
        setContentView(root)
        applyFullscreen()
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

    // Gamepad fisik tetap didukung berdampingan dengan kontrol sentuh
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
