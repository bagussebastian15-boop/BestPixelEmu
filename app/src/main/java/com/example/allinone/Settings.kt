package com.example.allinone

import android.content.Context
import androidx.appcompat.app.AlertDialog

object Settings {
    private fun prefs(c: Context) = c.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun fullscreen(c: Context) = prefs(c).getBoolean("fullscreen", true)
    fun stretch(c: Context) = prefs(c).getBoolean("stretch", true)
    fun showPad(c: Context) = prefs(c).getBoolean("show_pad", true)
    fun haptic(c: Context) = prefs(c).getBoolean("haptic", true)

    fun showDialog(c: Context, onDone: () -> Unit = {}) {
        val labels = arrayOf(
            "Layar penuh (sembunyikan status bar & tombol navigasi)",
            "Perbesar gambar game (isi layar, tanpa bar hitam)",
            "Tampilkan kontrol sentuh",
            "Getar saat tombol ditekan (berlaku di game berikutnya)"
        )
        val keys = arrayOf("fullscreen", "stretch", "show_pad", "haptic")
        val checked = booleanArrayOf(fullscreen(c), stretch(c), showPad(c), haptic(c))
        AlertDialog.Builder(c)
            .setTitle("Pengaturan")
            .setMultiChoiceItems(labels, checked) { _, i, isChecked ->
                prefs(c).edit().putBoolean(keys[i], isChecked).apply()
            }
            .setPositiveButton("Selesai") { _, _ -> onDone() }
            .show()
    }
}
