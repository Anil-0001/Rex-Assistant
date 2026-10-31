package com.personal.assistant.core.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class RexTextToSpeech(
    private val context: Context,
    private val onInitComplete: ((Boolean) -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    var isSpeakingCallback: ((Boolean) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default locale if US is missing
                tts?.setLanguage(Locale.getDefault())
            }

            // J.A.R.V.I.S.-inspired audio calibration:
            // Crisp, calm, slightly lowered pitch with clear delivery
            tts?.setPitch(0.92f)
            tts?.setSpeechRate(0.98f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    isSpeakingCallback?.invoke(true)
                }

                override fun onDone(utteranceId: String?) {
                    isSpeakingCallback?.invoke(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    isSpeakingCallback?.invoke(false)
                }
            })

            isInitialized = true
            Log.d("RexTTS", "R.E.X. TTS Engine initialized successfully.")
            onInitComplete?.invoke(true)
        } else {
            isInitialized = false
            Log.e("RexTTS", "Failed to initialize R.E.X. TTS Engine.")
            onInitComplete?.invoke(false)
        }
    }

    fun speak(text: String, flushPrevious: Boolean = true) {
        if (!isInitialized || tts == null) {
            Log.w("RexTTS", "TTS not initialized yet. Skipping speech.")
            return
        }

        // Clean text: strip emojis, brackets and tech markdown symbols for natural speech
        val cleanedText = cleanForSpeech(text)
        if (cleanedText.isBlank()) return

        val queueMode = if (flushPrevious) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "REX_UTTERANCE_${System.currentTimeMillis()}")
        }

        tts?.speak(cleanedText, queueMode, params, "REX_UTTERANCE_${System.currentTimeMillis()}")
    }

    fun stop() {
        if (isInitialized) {
            tts?.stop()
            isSpeakingCallback?.invoke(false)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun cleanForSpeech(input: String): String {
        return input
            // Remove emojis
            .replace("[\\p{So}\\p{Cn}]".toRegex(), "")
            // Remove markdown asterisks, hashes, backticks
            .replace("[*#`_~]".toRegex(), "")
            // Replace arrows or technical bullets
            .replace("->", " ")
            .replace("|", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
