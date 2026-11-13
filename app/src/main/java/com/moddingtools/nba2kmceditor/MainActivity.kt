package com.moddingtools.nba2kmceditor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.security.MessageDigest
import kotlin.math.log2

class MainActivity : ComponentActivity() {
    private var loadedBytes: ByteArray? = null
    private var loadedName: String = "No file loaded"
    private lateinit var status: TextView
    private lateinit var report: TextView
    private lateinit var hex: TextView
    private lateinit var saveButton: Button

    private val openFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) loadSafFile(uri)
    }
    private val exportFile = registerForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) exportSafFile(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 24)
            setBackgroundColor(0xFF101114.toInt())
        }
        fun label(text: String, size: Float = 14f) = TextView(this).apply {
            this.text = text; textSize = size; setTextColor(0xFFF2F2F2.toInt()); setPadding(0, 8, 0, 8)
        }
        root.addView(label("NBA2K MC EDITOR", 24f))
        root.addView(label("MyCAREER save inspector • v0.1.0"))
        val open = Button(this).apply { text = "LOAD .XUE / .OFF / SAVE FILE"; setOnClickListener { openFile.launch(arrayOf("*/*")) } }
        root.addView(open)
        status = label("No file loaded")
        root.addView(status)
        report = label("File analysis will appear here.")
        root.addView(report)
        root.addView(label("HEX PREVIEW (first 256 bytes)", 16f))
        hex = label("", 11f).apply { typeface = android.graphics.Typeface.MONOSPACE }
        val scroll = ScrollView(this)
        scroll.addView(hex)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        root.addView(label("EDITOR SECTIONS", 16f))
        root.addView(label("Cyber Face  •  Accessories  •  Signatures\nLocked until the save format and field offsets are verified."))
        saveButton = Button(this).apply { text = "EXPORT UNMODIFIED COPY"; isEnabled = false; setOnClickListener { exportFile.launch(safeName(loadedName)) } }
        root.addView(saveButton)
        root.addView(label("Safety: loading creates a private .bak copy. Unknown fields are never changed."))
        setContentView(root)
    }

    private fun loadSafFile(uri: Uri) {
        try {
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Could not read selected document")
            if (bytes.isEmpty()) throw IllegalArgumentException("The selected file is empty")
            val name = queryDisplayName(uri) ?: "career_save.bin"
            val backup = File(filesDir, "backups").apply { mkdirs() }
            File(backup, "${System.currentTimeMillis()}_${safeName(name)}.bak").writeBytes(bytes)
            loadedBytes = bytes
            loadedName = name
            status.text = "Loaded: $name (${bytes.size} bytes) • private backup created"
            report.text = analyze(bytes)
            hex.text = bytes.take(256).chunked(16).mapIndexed { row, chunk ->
                val offset = row * 16
                offset.toString(16).padStart(8, '0') + "  " + chunk.joinToString(" ") { "%02X".format(it) }
            }.joinToString("\n")
            saveButton.isEnabled = true
        } catch (e: Exception) {
            status.text = "Load failed: ${e.message ?: "unknown error"}"
            saveButton.isEnabled = false
        }
    }

    private fun analyze(b: ByteArray): String {
        val counts = IntArray(256)
        b.forEach { counts[it.toInt() and 0xff]++ }
        val n = b.size.toDouble()
        val entropy = counts.filter { it > 0 }.sumOf { c -> val p = c / n; -p * log2(p) }
        val digest = MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { "%02x".format(it) }
        val signature = b.take(16).joinToString(" ") { "%02X".format(it) }
        return "Size: ${b.size} bytes\nSHA-256: $digest\nByte entropy: ${"%.5f".format(entropy)} bits/byte\nFirst 16 bytes: $signature\n\nInterpretation: high entropy can indicate encryption, compression, or obfuscation. It does not prove which one. No player-field offsets have been assumed."
    }

    private fun exportSafFile(uri: Uri) {
        try {
            val bytes = loadedBytes ?: return
            contentResolver.openOutputStream(uri, "w")?.use { it.write(bytes) }
                ?: throw IllegalStateException("Could not open export destination")
            status.text = "Exported unchanged copy successfully."
        } catch (e: Exception) {
            status.text = "Export failed: ${e.message ?: "unknown error"}"
        }
    }

    private fun queryDisplayName(uri: Uri): String? = try {
        contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    } catch (_: Exception) { null }

    private fun safeName(name: String): String = name.replace(Regex("[^A-Za-z0-9._-]"), "_").take(100).ifBlank { "career_save.bin" }
}
