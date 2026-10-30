package com.personal.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.personal.assistant.R
import com.personal.assistant.core.actions.ActionResult
import com.personal.assistant.core.actions.AndroidActionExecutor
import com.personal.assistant.core.router.DeterministicRouter
import com.personal.assistant.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var executor: AndroidActionExecutor
    private lateinit var router: DeterministicRouter

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                Toast.makeText(this, "R.E.X. Audio Core authorized!", Toast.LENGTH_SHORT).show()
                displayResponse("🎙️ R.E.X. Audio Core active!\nListening channel open. Ready for Phase 3 speech engine.")
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

        setupUI()
        startOrbPulseAnimation()

        // Asynchronously warm-up the installed apps index in background
        Thread {
            executor.indexInstalledApps()
        }.start()
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
            val apps = executor.indexInstalledApps()
            handleResult(ActionResult.Success(
                message = "R.E.X. indexed ${apps.size} launchable apps.",
                details = "Sample apps: " + apps.take(10).joinToString { it.label }
            ))
        }

        binding.btnWifi.setOnClickListener {
            handleResult(executor.openWifiSettings())
        }

        binding.btnSafetyDemo.setOnClickListener {
            handleResult(executor.requestClearDataSimulation())
        }

        // Text command input
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

        // Glowing Mic button
        binding.btnMic.setOnClickListener {
            checkAndRequestAudio()
        }
    }

    private fun processCommand(command: String) {
        binding.tvStatus.text = "R.E.X. // EXECUTING..."
        binding.tvStatus.setTextColor(getColor(R.color.ember_orange))

        val result = router.execute(command)
        handleResult(result)

        binding.tvStatus.postDelayed({
            binding.tvStatus.text = getString(R.string.status_idle)
            binding.tvStatus.setTextColor(getColor(R.color.crimson_red))
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
            }
            is ActionResult.RequiresConfirmation -> {
                showSafetyConfirmationModal(result)
            }
            is ActionResult.Error -> {
                displayResponse("❌ ${result.reason}")
            }
        }
    }

    private fun showSafetyConfirmationModal(confirmation: ActionResult.RequiresConfirmation) {
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
            displayResponse("🛡️ Security Alert: Action [${confirmation.actionId}] aborted by user.")
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

    private fun checkAndRequestAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            displayResponse("🎙️ R.E.X. Crimson Audio Core active!\nListening channel open. Tap quick chips or type commands.")
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startOrbPulseAnimation() {
        val pulse = AlphaAnimation(0.2f, 0.7f).apply {
            duration = 1200
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.orbOuterRing.startAnimation(pulse)
    }
}
