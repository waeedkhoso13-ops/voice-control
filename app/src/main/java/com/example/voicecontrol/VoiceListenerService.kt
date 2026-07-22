package com.example.voicecontrol

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Runs in the background so the phone can be controlled by voice
 * without opening the app or touching the phone — fully offline via
 * OfflineVoiceEngine (Vosk). Android requires a visible notification
 * whenever an app's microphone runs in the background — this is an
 * OS-level transparency rule, not optional.
 */
class VoiceListenerService : Service() {

    private val channelId = "voice_control_channel"
    private var engine: OfflineVoiceEngine? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, buildNotification("Offline model load ho raha hai..."))
        engine = OfflineVoiceEngine(
            context = this,
            onCommand = { command -> CommandHandler.handleCommand(this, command) },
            onStatus = { status -> updateNotification(status) }
        )
        engine?.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Raza (always listening)",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Raza (offline)")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(1, buildNotification(text))
    }

    override fun onDestroy() {
        engine?.stop()
        engine = null
        super.onDestroy()
    }
}
