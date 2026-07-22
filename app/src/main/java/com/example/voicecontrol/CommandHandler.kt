package com.example.voicecontrol

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import androidx.core.content.ContextCompat

object CommandHandler {

    var onLog: ((String) -> Unit)? = null

    private fun log(message: String) {
        onLog?.invoke(message)
    }

    fun handleCommand(context: Context, command: String) {
        TtsHelper.init(context)
        when {
            command.contains("hello raza") || command.contains("hi raza") ||
                command.contains("हेलो राज़ा") || command.contains("हेलो राजा") ||
                command.contains("हैलो राज़ा") || command.contains("हैलो राजा") ||
                command.contains("राज़ा") || command.contains("राजा") ->
                respondToGreeting(context)
            command.contains("flashlight") || command.contains("torch") ||
                command.contains("टॉर्च") || command.contains("फ्लैश") ->
                if (command.contains("off") || command.contains("बंद")) {
                    toggleFlashlight(context, false)
                } else {
                    toggleFlashlight(context, true)
                }
            command.contains("call") || command.contains("कॉल") -> handleCall(context, command)
            command.contains("message") || command.contains("sms") ||
                command.contains("मैसेज") || command.contains("संदेश") -> handleSms(context, command)
            command.contains("whatsapp") || command.contains("व्हाट्सएप") -> openApp(context, "com.whatsapp")
            command.contains("camera") || command.contains("कैमरा") -> openCamera(context)
            command.contains("volume up") || command.contains("आवाज़ बढ़ा") || command.contains("आवाज बढ़ा") ->
                adjustVolume(context, true)
            command.contains("volume down") || command.contains("आवाज़ कम") || command.contains("आवाज कम") ->
                adjustVolume(context, false)
            command.contains("search") || command.contains("खोज") || command.contains("सर्च") ->
                webSearch(context, command)
            else -> log("Ye command samajh nahi aaya: $command")
        }
    }

    private fun respondToGreeting(context: Context) {
        val replies = listOf("जी बोलिए", "जी फरमाइए", "हाँ मैं सुन रहा हूँ")
        val reply = replies.random()
        TtsHelper.speak(reply)
        log("Raza ne jawab diya: $reply")
    }

    private fun toggleFlashlight(context: Context, turnOn: Boolean) {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, turnOn)
            log(if (turnOn) "Flashlight on ki gayi" else "Flashlight off ki gayi")
        } catch (e: Exception) {
            log("Flashlight control fail: ${e.message}")
        }
    }

    private fun handleCall(context: Context, command: String) {
        val number = command.filter { it.isDigit() }
        if (number.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                context.startActivity(intent)
                log("Call kiya jaa raha hai: $number")
            } else {
                log("Call permission nahi hai.")
            }
        } else {
            log("Number samajh nahi aaya. Number bol kar dubara koshish karein.")
        }
    }

    private fun handleSms(context: Context, command: String) {
        val number = command.filter { it.isDigit() }
        if (number.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            log("Message app khola gaya: $number")
        } else {
            log("Number samajh nahi aaya.")
        }
    }

    private fun openApp(context: Context, packageName: String) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            log("$packageName khola gaya")
        } else {
            log("App install nahi hai: $packageName")
        }
    }

    private fun openCamera(context: Context) {
        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            log("Camera khola gaya")
        }
    }

    private fun adjustVolume(context: Context, up: Boolean) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (up) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
        log(if (up) "Volume badhaya gaya" else "Volume kam kiya gaya")
    }

    private fun webSearch(context: Context, command: String) {
        val query = command.replace("search", "").trim()
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra("query", query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            log("Search kiya gaya: $query")
        } catch (e: Exception) {
            log("Search open nahi ho saka")
        }
    }
}
