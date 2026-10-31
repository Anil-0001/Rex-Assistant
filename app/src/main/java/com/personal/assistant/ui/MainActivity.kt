package com.personal.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.personal.assistant.R
import com.personal.assistant.core.actions.ActionResult
import com.personal.assistant.core.actions.AndroidActionExecutor
import com.personal.assistant.core.router.DeterministicRouter
import com.personal.assistant.core.voice.RexSpeechRecognizer
import com.personal.assistant.core.voice.RexTextToSpeech
import com.personal.assistant.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: AndroidActionExecutor
    private lateinit var router: DeterministicRouter

    // Phase 3 Voice Pipeline
    private lateinit var tts: RexTextToSpeech
    private lateinit var speechRecognizer: RexSpeechRecognizer
    private var isListening = false

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(this, "R.E.X. Audio Core authorized!", Toast.LENGTH_SHORT).show()
                startVoiceInput()
            } else {
                displayResponse("Microphone permission was denied. Tap mic to retry.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        executor = AndroidActionExecutor(this)
        router = DeterministicRouter(executor)

        initVoiceEngines()
        setupUI()
        startOrbPulseAnimation()

        // Asynchronously warm-up installed apps index in background coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            executor.indexInstalledApps()
        }
    }

    private fun initVoiceEngines() {
        // 1. Text-To-Speech Initialization
        tts = RexTextToSpeech(this) { success ->
            runOnUiThread {
                if (success) {
                    binding.tvStatus.text = "R.E.X. // VOICE SYNTHESIS READY"
                    binding.tvStatus.postDelayed({
                        if (!isListening) {
                            binding.tvStatus.text = getString(R.string.status_idle)
                            binding.tvStatus.setTextColor(getColor(R.color.crimson_red))
                        }
                    }, 2000)
                }
            }
        }

        // Animate Arc Reactor orb when R.E.X. is speaking
        tts.isSpeakingCallback = { speaking ->
            runOnUiThread {
                if (speaking) {
                    animateOrbSpeaking(true)
                } else {
                    animateOrbSpeaking(false)
                }
            }
        }

        // 2. Speech-To-Text Recognizer Initialization
        speechRecognizer = RexSpeechRecognizer(
            context = this,
            onResult = { recognizedText ->
                runOnUiThread {
                    binding.etCommand.setText(recognizedText)
                    processCommand(recognizedText)
                }
            },
            onError = { errorMessage ->
                runOnUiThread {
                    displayResponse("⚠️ $errorMessage")
                    tts.speak(errorMessage)
                }
            },
            onListeningStateChanged = { listening ->
                runOnUiThread {
                    isListening = listening
                    updateListeningUI(listening)
                }
            }
        )
    }

    private fun setupUI() {
        // Quick Action Chips
        binding.btnBattery.setOnClickListener {
            handleResult(executor.getBatteryStatus())
        }

        binding.btnStorage.setOnClickListener {
            handleResult(executor.getStorageStatus())
        }

        binding.btnNetwork.setOnClickListener {
            handleResult(executor.getNetworkStatus())
        }

        binding.btnAppScan.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val apps = executor.indexInstalledApps()
                val result = ActionResult.Success(
                    message = "R.E.X. indexed ${apps.size} launchable apps.",
                    details = "Sample apps: " + apps.take(10).joinToString { it.label }
                )
                runOnUiThread {
                    handleResult(result)
                }
            }
        }

        binding.btnWifi.setOnClickListener {
            handleResult(executor.openWifiSettings())
        }

        binding.btnSafetyDemo.setOnClickListener {
            handleResult(executor.requestClearDataSimulation())
        }

        // Text command input (Keyboard Enter)
        binding.etCommand.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                val query = binding.etCommand.text.toString().trim()
                if (query.isNotEmpty()) {
                    processCommand(query)
                    binding.etCommand.text.clear()
                }
                true
            } else {
                false
            }
        }

        // Tap-to-Talk Glowing Mic button
        binding.btnMic.setOnClickListener {
            if (isListening) {
                speechRecognizer.stopListening()
            } else {
                checkAndStartVoiceInput()
            }
        }
    }

    private fun checkAndStartVoiceInput() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startVoiceInput()
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceInput() {
        tts.stop() // Stop speaking when user wants to talk
        speechRecognizer.startListening()
    }

    private fun updateListeningUI(listening: Boolean) {
        if (listening) {
            binding.tvStatus.text = "R.E.X. // LISTENING..."
            binding.tvStatus.setTextColor(getColor(R.color.ember_orange))
            binding.btnMic.animate().scaleX(1.15f).scaleY(1.15f).setDuration(250).start()
        } else {
            binding.btnMic.animate().scaleX(1.0f).scaleY(1.0f).setDuration(250).start()
            binding.tvStatus.postDelayed({
                if (!isListening) {
                    binding.tvStatus.text = getString(R.string.status_idle)
                    binding.tvStatus.setTextColor(getColor(R.color.crimson_red))
                }
            }, 1000)
        }
    }

    private fun processCommand(command: String) {
        binding.tvStatus.text = "R.E.X. // EXECUTING..."
        binding.tvStatus.setTextColor(getColor(R.color.ember_orange))

        val result = router.execute(command)
        handleResult(result)

        binding.tvStatus.postDelayed({
            if (!isListening) {
                binding.tvStatus.text = getString(R.string.status_idle)
                binding.tvStatus.setTextColor(getColor(R.color.crimson_red))
            }
        }, 1500)
    }

    private fun handleResult(result: ActionResult) {
        when (result) {
            is ActionResult.Success -> {
                val fullMsg = buildString {
                    append("✅ ")
                    append(result.message)
                    if (!result.details.isNullOrEmpty()) {
                        append("\n\nTelemetry:\n")
                        append(result.details)
                    }
                }
                displayResponse(fullMsg)
                // Spoken feedback for success
                tts.speak(result.message)
            }
            is ActionResult.RequiresConfirmation -> {
                showSafetyConfirmationModal(result)
            }
            is ActionResult.Error -> {
                displayResponse("❌ ${result.reason}")
                tts.speak(result.reason)
            }
        }
    }

    private fun showSafetyConfirmationModal(confirmation: ActionResult.RequiresConfirmation) {
        // Spoken prompt for security confirmation
        tts.speak("Sensitive action requested. Please confirm on screen.")

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvModalTitle)
        val tvActionId = dialogView.findViewById<TextView>(R.id.tvModalActionId)
        val tvPrompt = dialogView.findViewById<TextView>(R.id.tvModalPrompt)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<android.widget.Button>(R.id.btnConfirm)

        tvTitle.text = confirmation.title
        tvActionId.text = "ACTION: ${confirmation.actionId}"
        tvPrompt.text = confirmation.prompt

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener {
            dialog.dismiss()
            val abortMsg = "Security Alert: Action ${confirmation.actionId} aborted by user."
            displayResponse("🛡️ $abortMsg")
            tts.speak("Action aborted.")
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            val executedResult = confirmation.onConfirm()
            handleResult(executedResult)
        }

        dialog.show()
    }

    private fun displayResponse(text: String) {
        binding.tvOutput.text = text
    }

    private fun startOrbPulseAnimation() {
        val pulse = AlphaAnimation(0.2f, 0.7f).apply {
            duration = 1200
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.orbOuterRing.startAnimation(pulse)
    }

    private fun animateOrbSpeaking(speaking: Boolean) {
        if (speaking) {
            val scale = ScaleAnimation(
                1.0f, 1.25f, 1.0f, 1.25f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 450
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }
            binding.orbCore.startAnimation(scale)
        } else {
            binding.orbCore.clearAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.stopListening()
        tts.shutdown()
    }
}
