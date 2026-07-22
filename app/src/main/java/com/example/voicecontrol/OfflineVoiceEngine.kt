package com.example.voicecontrol

import android.content.Context
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.StorageService

/**
 * Fully offline speech recognition using Vosk — no internet connection
 * is ever used or needed. The model is bundled inside the app itself
 * (see README for how to add the model files).
 */
class OfflineVoiceEngine(
    private val context: Context,
    private val onCommand: (String) -> Unit,
    private val onStatus: (String) -> Unit
) {
    private var model: Model? = null
    private var speechService: SpeechService? = null

    fun start() {
        onStatus("Offline model load ho raha hai...")
        StorageService.unpack(
            context, "model-en-us", "model",
            { loadedModel ->
                model = loadedModel
                startRecognizer()
            },
            { exception ->
                onStatus(
                    "Offline model nahi mila. README ke mutabiq model " +
                        "assets/model-en-us folder mein daalein. (${exception.message})"
                )
            }
        )
    }

    private fun startRecognizer() {
        val currentModel = model ?: return
        val recognizer = Recognizer(currentModel, 16000.0f)
        speechService = SpeechService(recognizer, 16000.0f)
        speechService?.startListening(listener)
        onStatus("Offline mode: sun raha hai (bina internet)")
    }

    private val listener = object : RecognitionListener {
        override fun onPartialResult(hypothesis: String?) {}

        override fun onResult(hypothesis: String?) {
            val text = extractText(hypothesis)
            if (text.isNotBlank()) onCommand(text)
        }

        override fun onFinalResult(hypothesis: String?) {
            val text = extractText(hypothesis)
            if (text.isNotBlank()) onCommand(text)
        }

        override fun onError(exception: Exception?) {
            onStatus("Recognition error: ${exception?.message}")
        }

        override fun onTimeout() {}
    }

    private fun extractText(hypothesis: String?): String {
        if (hypothesis.isNullOrBlank()) return ""
        return try {
            JSONObject(hypothesis).optString("text", "")
        } catch (e: Exception) {
            ""
        }
    }

    fun stop() {
        speechService?.stop()
        speechService?.shutdown()
        speechService = null
        model = null
    }
}
