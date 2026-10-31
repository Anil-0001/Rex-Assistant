package com.personal.assistant.core.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

class RexSpeechRecognizer(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening() {
        if (!isAvailable()) {
            onError("Speech recognition service is not available on this device.")
            return
        }

        stopListening()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    onListeningStateChanged(true)
                }

                override fun onBeginningOfSpeech() {
                    // User has started talking
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Audio sound level fluctuations can be tracked here
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    isListening = false
                    onListeningStateChanged(false)
                }

                override fun onError(errorCode: Int) {
                    isListening = false
                    onListeningStateChanged(false)
                    val message = getErrorMessage(errorCode)
                    Log.w("RexSTT", "Speech recognition error: $message ($errorCode)")
                    onError(message)
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    onListeningStateChanged(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val bestQuery = matches?.firstOrNull()?.trim() ?: ""
                    if (bestQuery.isNotEmpty()) {
                        onResult(bestQuery)
                    } else {
                        onError("Could not understand voice. Please try again.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {}

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Support Indian English + Hindi voice input for Hinglish phrases like "whatsapp kholo"
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-US", "hi"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Listening for R.E.X. command...")
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isListening = false
            onListeningStateChanged(false)
            onError("Failed to start speech recognition: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (ignored: Exception) {}
            isListening = false
            onListeningStateChanged(false)
        }
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
            SpeechRecognizer.ERROR_CLIENT -> "Client error occurred."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient audio permissions."
            SpeechRecognizer.ERROR_NETWORK -> "Network connection error."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network operation timed out."
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking closer to mic."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy. Retrying..."
            SpeechRecognizer.ERROR_SERVER -> "Recognition server error."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Timeout."
            else -> "Speech recognition error code: $errorCode"
        }
    }
}
