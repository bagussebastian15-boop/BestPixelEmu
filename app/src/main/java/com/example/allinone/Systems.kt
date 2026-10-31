package com.example.allinone

enum class GameSystem(val label: String, val coreLib: String, val exts: Set<String>) {
    GBA("Game Boy Advance", "libmgba_libretro_android.so", setOf("gba")),
    GENESIS("Sega Genesis", "libgenesis_plus_gx_libretro_android.so", setOf("md", "gen", "smd", "bin")),
    SNES("Super Nintendo", "libsnes9x_libretro_android.so", setOf("sfc", "smc")),
    ARCADE("Arcade (FBNeo)", "libfbneo_libretro_android.so", setOf("zip"));

    companion object {
        fun fromFileName(name: String): GameSystem? {
            val ext = name.substringAfterLast('.', "").lowercase()
            return values().firstOrNull { ext in it.exts }
        }
    }
}
