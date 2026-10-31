package com.example.allinone

import android.content.Context
import androidx.appcompat.app.AlertDialog

object Settings {
    private fun prefs(c: Context) = c.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun fullscreen(c: Context) = prefs(c).getBoolean("fullscreen", true)
    fun showPad(c: Context) = prefs(c).getBoolean("show_pad", true)
    fun haptic(c: Context) = prefs(c).getBoolean("haptic", true)

    fun showDialog(c: Context) {
        val labels = arrayOf(
            "Layar penuh (sembunyikan status bar & tombol navigasi)",
            "Tampilkan kontrol sentuh",
            "Getar saat tombol ditekan"
        )
        val keys = arrayOf("fullscreen", "show_pad", "haptic")
        val checked = booleanArrayOf(fullscreen(c), showPad(c), haptic(c))
        AlertDialog.Builder(c)
            .setTitle("Pengaturan")
            .setMultiChoiceItems(labels, checked) { _, i, isChecked ->
                prefs(c).edit().putBoolean(keys[i], isChecked).apply()
            }
            .setPositiveButton("Selesai", null)
            .show()
    }
}
