package com.example.allinone

import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.swordfish.libretrodroid.GLRetroView
import com.swordfish.libretrodroid.GLRetroViewData

class GameActivity : AppCompatActivity() {

    private lateinit var retroView: GLRetroView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = android.net.Uri.parse(intent.getStringExtra("uri"))
        val core = intent.getStringExtra("core")!!
        val bytes = contentResolver.openInputStream(uri)!!.use { it.readBytes() }

        val data = GLRetroViewData(this).apply {
            coreFilePath = core
            gameFileBytes = bytes
            systemDirectory = filesDir.absolutePath
            savesDirectory = getExternalFilesDir("saves")!!.absolutePath
            shader = com.swordfish.libretrodroid.ShaderConfig.Default
        }
        retroView = GLRetroView(this, data)
        lifecycle.addObserver(retroView)
        setContentView(retroView)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.source and android.view.InputDevice.SOURCE_GAMEPAD != 0) {
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
