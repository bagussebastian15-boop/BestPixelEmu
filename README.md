# Emulator All-in-One (Android 5.0+)

Frontend emulator berbasis **libretro** + [LibretroDroid](https://github.com/Swordfish90/LibretroDroid).
Sistem: GBA (mGBA), SNES (Snes9x), Sega Genesis/Mega Drive (Genesis Plus GX),
Arcade (FBNeo dan MAME 2003-Plus). Gratis, tanpa iklan, ROM milik pengguna sendiri.

## Cara build

### Opsi A: GitHub Actions (tanpa install apa pun)
1. Push folder ini ke repo GitHub.
2. Tab **Actions** -> workflow **Build APK** jalan otomatis (atau *Run workflow*).
3. Unduh APK dari **Artifacts** (`EmulatorAllInOne-debug-apk`).

### Opsi B: Android Studio
1. Jalankan `bash scripts/fetch_cores.sh` (mengunduh core ke `app/src/main/jniLibs/`).
2. Buka folder ini di Android Studio -> Run.

## Fitur
| Fitur | Implementasi |
|---|---|
| Save/Load state | 5 slot + slot otomatis (disimpan saat pause/keluar); SRAM in-game disimpan terpisah |
| Controller map & plug n play | Deteksi gamepad otomatis, port pemain 1-4, peta tombol per perangkat, kontrol layar otomatis sembunyi saat gamepad terhubung |
| Upscaling & grafis | Skala layar penuh + shader Standar / Tajam / CRT / LCD |
| API grafis | OpenGL ES 2.0+ (lihat catatan Vulkan) |
| Fast forward & frame skip | 2x/3x/4x/8x; frame skip (mGBA) + lewati frame duplikat |
| Shader & filter retro | Pilih preset dari menu game dan pengaturan |
| Cheat | Per game, tambah/aktif/hapus; format kode mengikuti core |
| Kontrol layar | Multi-touch, transparansi dan getar bisa diatur |

## Catatan jujur (perlu dicek)
- **Belum di-build/diuji** di lingkungan pembuatan (tanpa Android SDK/jaringan). Jika ada error kompilasi,
  hampir pasti di nama API LibretroDroid yang berubah antarversi: ubah `libretroDroidVersion`
  di `app/build.gradle.kts` (lihat jitpack.io/#Swordfish90/LibretroDroid) dan sesuaikan
  `EmulatorActivity.kt` / `Settings.kt` (`ShaderConfig`, `setCheat`, `getGLRetroEvents`).
- **Vulkan** belum disertakan: hanya OpenGL ES. Perlu versi LibretroDroid yang mendukung Vulkan.
- **Upscaling**: core 2D punya resolusi internal tetap, jadi "upscale" = skala + shader, bukan render ulang.
- **Shader**: hanya preset bawaan LibretroDroid; file `.glslp` kustom tidak didukung.
- **Frame skip** per core: baru mGBA (`mgba_frameskip`). Core lain bisa ditambah di `EmulatorActivity.onCreate`.
- **Arcade** butuh set ROM yang cocok dengan versi core (FBNeo vs MAME 2003-Plus). Coba core lain lewat
  tekan-lama pada game. BIOS (mis. `neogeo.zip`) diimpor sebagai file zip biasa ke folder yang sama.
- **Lisensi core**: Snes9x, Genesis Plus GX, FBNeo, MAME 2003-Plus berlisensi non-komersial; aplikasi ini
  hanya boleh dibagikan gratis. Cek lisensi masing-masing core sebelum merilis.

## Struktur
```
app/src/main/java/com/allinone/emulator/
  data/   Console, Paths, RomLibrary, Settings, CheatStore
  input/  KeyMap, VirtualPadView
  ui/     LibraryActivity, EmulatorActivity, Menus
scripts/fetch_cores.sh        unduh core libretro
.github/workflows/build.yml   build APK otomatis
```
