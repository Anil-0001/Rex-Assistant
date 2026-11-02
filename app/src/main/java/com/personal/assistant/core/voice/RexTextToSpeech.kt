package com.personal.assistant.core.voice

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

/**
 * R.E.X. J.A.R.V.I.S. Deep Male Voice Engine.
 * Explicitly forces deep male resonance:
 * - Pitch set to 0.72f (Heavy, authoritative, deep masculine tone).
 * - Rate set to 0.92f (Calm, collected British Jarvis tempo).
 * - Enforces selection of male voice pack from Google TTS engine.
 */
class RexTextToSpeech(
    private val context: Context,
    private val onInitComplete: ((Boolean) -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    var isSpeakingCallback: ((Boolean) -> Unit)? = null

    init {
        // Initialize Google TTS engine explicitly if available
        tts = TextToSpeech(context.applicationContext, this, "com.google.android.tts")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            setupJarvisVoice()

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
            Log.d("RexTTS", "R.E.X. Deep Male Jarvis Voice online.")
            onInitComplete?.invoke(true)
        } else {
            // Fallback to default system TTS engine if Google TTS package is absent
            tts = TextToSpeech(context.applicationContext) { fallbackStatus ->
                if (fallbackStatus == TextToSpeech.SUCCESS) {
                    setupJarvisVoice()
                    isInitialized = true
                    onInitComplete?.invoke(true)
                } else {
                    isInitialized = false
                    onInitComplete?.invoke(false)
                }
            }
        }
    }

    private fun setupJarvisVoice() {
        val ttsEngine = tts ?: return

        // Set Language to UK / English
        val locales = listOf(Locale.UK, Locale("en", "GB"), Locale.US, Locale.getDefault())
        for (loc in locales) {
            val r = ttsEngine.setLanguage(loc)
            if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) {
                break
            }
        }

        // Deep Jarvis Frequency Tuning (Forces heavy male baritone sound regardless of default voice)
        // 0.70f - 0.75f lowers the vocal tract dramatically to a deep male tone
        ttsEngine.setPitch(0.72f)
        ttsEngine.setSpeechRate(0.92f)

        try {
            val voices = ttsEngine.voices
            if (!voices.isNullOrEmpty()) {
                // Priority known Google Male voice names: rjs, fis, sfg, etc.
                val maleNames = listOf("en-gb-x-rjs", "en-gb-x-fis", "en-us-x-sfg", "en-us-x-iol", "male")
                val matchedMale = voices.firstOrNull { v ->
                    val n = v.name.lowercase()
                    maleNames.any { n.contains(it) }
                }

                if (matchedMale != null) {
                    ttsEngine.voice = matchedMale
                    Log.d("RexTTS", "Locked Male Voice: ${matchedMale.name}")
                } else {
                    // Filter out any voice with female indicators
                    val nonFemale = voices.firstOrNull { v ->
                        val n = v.name.lowercase()
                        v.locale.language == "en" && !n.contains("female") && !n.contains("woman") && !n.contains("fem")
                    }
                    if (nonFemale != null) {
                        ttsEngine.voice = nonFemale
                        Log.d("RexTTS", "Selected Non-Female Voice: ${nonFemale.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("RexTTS", "Voice inspection fallback: ${e.message}")
        }
    }

    fun speak(text: String, flushPrevious: Boolean = true) {
        if (!isInitialized || tts == null) {
            Log.w("RexTTS", "TTS not initialized yet. Skipping speech.")
            return
        }

        val cleanedText = cleanForSpeech(text)
        if (cleanedText.isBlank()) return

        val queueMode = if (flushPrevious) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "REX_${System.currentTimeMillis()}")
            // Audio stream for clean voice playback
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        tts?.speak(cleanedText, queueMode, params, "REX_${System.currentTimeMillis()}")
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
            .replace("[*#`_~]".toRegex(), "")
            .replace("\\[.*?\\]".toRegex(), "")
            .replace("\\(.*?\\)".toRegex(), "")
            .replace("[\\p{So}\\p{Cn}]".toRegex(), "")
            .replace("->", " ")
            .replace("|", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
