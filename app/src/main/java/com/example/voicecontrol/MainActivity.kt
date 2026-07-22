package com.example.voicecontrol

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var micButton: Button
    private lateinit var alwaysListenButton: Button
    private lateinit var statusText: TextView
    private lateinit var logText: TextView

    private var alwaysListening = false
    private var localListening = false
    private var localEngine: OfflineVoiceEngine? = null

    private val requiredPermissions: Array<String> by lazy {
        val base = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            base.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        base.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        micButton = findViewById(R.id.micButton)
        alwaysListenButton = findViewById(R.id.alwaysListenButton)
        statusText = findViewById(R.id.statusText)
        logText = findViewById(R.id.logText)

        requestNeededPermissions()

        // Route every recognized command (from this screen or the
        // background service) into the same log view.
        CommandHandler.onLog = { message -> runOnUiThread { appendLog(message) } }

        micButton.text = "🎤\nShuru"
        micButton.setOnClickListener { toggleLocalListening() }

        alwaysListenButton.setOnClickListener { toggleAlwaysListening() }
    }

    private fun toggleLocalListening() {
        localListening = !localListening
        if (localListening) {
            micButton.text = "🎤\nRoko"
            localEngine = OfflineVoiceEngine(
                context = this,
                onCommand = { command ->
                    runOnUiThread {
                        appendLog("Suna: $command")
                        CommandHandler.handleCommand(this, command)
                    }
                },
                onStatus = { status -> runOnUiThread { statusText.text = status } }
            )
            localEngine?.start()
        } else {
            micButton.text = "🎤\nShuru"
            localEngine?.stop()
            localEngine = null
            statusText.text = "Boliye, jaise: \"flashlight on karo\""
        }
    }

    private fun toggleAlwaysListening() {
        alwaysListening = !alwaysListening
        if (alwaysListening) {
            val serviceIntent = Intent(this, VoiceListenerService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
            alwaysListenButton.text = "Hamesha sunna: ON"
            appendLog("Background listening shuru ho gayi — ab phone uthaye bina bhi command chalengi (offline).")
        } else {
            stopService(Intent(this, VoiceListenerService::class.java))
            alwaysListenButton.text = "Hamesha sunna: OFF"
            appendLog("Background listening band kar di gayi.")
        }
    }

    private fun requestNeededPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        }
    }

    private fun appendLog(text: String) {
        logText.append("\n$text")
    }

    override fun onDestroy() {
        super.onDestroy()
        localEngine?.stop()
    }
}
