package com.personal.assistant.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
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
import com.personal.assistant.core.brain.GeminiCloudBrain
import com.personal.assistant.core.router.DeterministicRouter
import com.personal.assistant.core.router.HybridOrchestrator
import com.personal.assistant.core.voice.RexSpeechRecognizer
import com.personal.assistant.core.voice.RexTextToSpeech
import com.personal.assistant.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: AndroidActionExecutor
    private lateinit var router: DeterministicRouter
    private lateinit var orchestrator: HybridOrchestrator
    private lateinit var cloudBrain: GeminiCloudBrain

    // Voice Engine Components (Phase 3)
    private lateinit var tts: RexTextToSpeech
    private lateinit var speechRecognizer: RexSpeechRecognizer
    private var isListening: Boolean = false

    private val prefs by lazy { getSharedPreferences("rex_prefs", Context.MODE_PRIVATE) }

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
        cloudBrain = GeminiCloudBrain()
        orchestrator = HybridOrchestrator(executor, router, cloudBrain)

        // Restore saved API Key
        val savedKey = prefs.getString("gemini_api_key", "") ?: ""
        if (savedKey.isNotEmpty()) {
            cloudBrain.setApiKey(savedKey)
        }

        setupVoiceEngine()
        setupUI()
        startOrbPulseAnimation()

        // Background indexing of apps for instant tool execution
        lifecycleScope.launch(Dispatchers.IO) {
            executor.indexInstalledApps()
        }
    }

    private fun setupVoiceEngine() {
        tts = RexTextToSpeech(this) { speaking ->
            runOnUiThread {
                animateOrbSpeaking(speaking)
            }
        }

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

        binding.btnBrainConfig.setOnClickListener {
            showApiKeyDialog()
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
        tts.stop()
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

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun processCommand(command: String) {
        binding.tvStatus.text = "R.E.X. // REASONING..."
        binding.tvStatus.setTextColor(getColor(R.color.ember_orange))

        val online = isNetworkConnected()

        lifecycleScope.launch(Dispatchers.IO) {
            val result = orchestrator.process(command, online)
            withContext(Dispatchers.Main) {
                handleResult(result)
                binding.tvStatus.postDelayed({
                    if (!isListening) {
                        binding.tvStatus.text = getString(R.string.status_idle)
                        binding.tvStatus.setTextColor(getColor(R.color.crimson_red))
                    }
                }, 1500)
            }
        }
    }

    private fun handleResult(result: ActionResult) {
        when (result) {
            is ActionResult.Success -> {
                val fullMsg = buildString {
                    append("✅ ")
                    append(result.message)
                    if (!result.details.isNullOrEmpty()) {
                        append("\n\nTelemetry / Context:\n")
                        append(result.details)
                    }
                }
                displayResponse(fullMsg)
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

    private fun showApiKeyDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_api_key, null)
        val etInput = dialogView.findViewById<EditText>(R.id.etApiKeyInput)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancelKey)
        val btnSave = dialogView.findViewById<android.widget.Button>(R.id.btnSaveKey)

        val currentKey = prefs.getString("gemini_api_key", "") ?: ""
        if (currentKey.isNotEmpty()) {
            etInput.setText(currentKey)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val entered = etInput.text.toString().trim()
            if (entered.length >= 20) {
                prefs.edit().putString("gemini_api_key", entered).apply()
                cloudBrain.setApiKey(entered)
                dialog.dismiss()
                val success = "Gemini 1.5 Flash Cognitive Brain connected successfully!"
                displayResponse("🧠 $success\nYou can now speak in natural conversational language!")
                tts.speak("AI Brain connected, boss.")
            } else {
                Toast.makeText(this, "Please enter a valid API key", Toast.LENGTH_LONG).show()
            }
        }

        dialog.show()
    }

    private fun showSafetyConfirmationModal(confirmation: ActionResult.RequiresConfirmation) {
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
