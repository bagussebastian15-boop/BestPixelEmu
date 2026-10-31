package com.example.allinone

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile

class MainActivity : AppCompatActivity() {

    private data class Rom(val title: String, val uri: Uri, val system: GameSystem)
    private val roms = mutableListOf<Rom>()
    private lateinit var adapter: ArrayAdapter<String>

    private val pickFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            getSharedPreferences("lib", MODE_PRIVATE).edit().putString("root", uri.toString()).apply()
            scan(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val folderButton = Button(this).apply {
            text = "Pilih folder ROM"
            setOnClickListener { pickFolder.launch(null) }
        }
        val settingsButton = Button(this).apply {
            text = "Pengaturan"
            setOnClickListener { Settings.showDialog(this@MainActivity) }
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        val list = ListView(this).apply {
            this.adapter = this@MainActivity.adapter
            setOnItemClickListener { _, _, pos, _ ->
                val rom = roms[pos]
                startActivity(
                    Intent(this@MainActivity, GameActivity::class.java)
                        .putExtra("uri", rom.uri.toString())
                        .putExtra("core", rom.system.coreLib)
                )
            }
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(folderButton)
            addView(settingsButton)
            addView(list)
        })

        getSharedPreferences("lib", MODE_PRIVATE).getString("root", null)?.let { scan(Uri.parse(it)) }
    }

    private fun scan(root: Uri) {
        roms.clear()
        fun walk(dir: DocumentFile) {
            for (f in dir.listFiles()) {
                if (f.isDirectory) walk(f)
                else f.name?.let { n ->
                    GameSystem.fromFileName(n)?.let { roms += Rom(n.substringBeforeLast('.'), f.uri, it) }
                }
            }
        }
        DocumentFile.fromTreeUri(this, root)?.let { walk(it) }
        roms.sortBy { it.title.lowercase() }
        adapter.clear()
        adapter.addAll(roms.map { "${it.title}  [${it.system.label}]" })
    }
}
