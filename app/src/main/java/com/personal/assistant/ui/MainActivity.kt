package com.personal.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
                Toast.makeText(this, "Microphone permission granted!", Toast.LENGTH_SHORT).show()
                displayResponse("Microphone ready. Speech-to-Text engine will activate in Phase 3.")
            } else {
                displayResponse("Microphone permission was denied. Tap to retry.")
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
    }

    private fun setupUI() {
        // Quick Actions
        binding.btnBattery.setOnClickListener {
            handleResult(executor.getBatteryStatus())
        }

        binding.btnWifi.setOnClickListener {
            handleResult(executor.openWifiSettings())
        }

        binding.btnDeviceInfo.setOnClickListener {
            handleResult(executor.getDeviceInfo())
        }

        binding.btnYouTube.setOnClickListener {
            handleResult(executor.launchAppByName("youtube"))
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
        binding.tvStatus.text = "EXECUTING..."
        binding.tvStatus.setTextColor(getColor(R.color.electric_blue))

        val result = router.execute(command)
        handleResult(result)

        binding.tvStatus.postDelayed({
            binding.tvStatus.text = getString(R.string.status_idle)
            binding.tvStatus.setTextColor(getColor(R.color.neon_cyan))
        }, 1500)
    }

    private fun handleResult(result: ActionResult) {
        when (result) {
            is ActionResult.Success -> {
                val fullMsg = buildString {
                    append("✅ ")
                    append(result.message)
                    if (!result.details.isNullOrEmpty()) {
                        append("\n\nDetails: ")
                        append(result.details)
                    }
                }
                displayResponse(fullMsg)
            }
            is ActionResult.RequiresConfirmation -> {
                displayResponse("⚠️ Confirmation Required: ${result.prompt}")
            }
            is ActionResult.Error -> {
                displayResponse("❌ ${result.reason}")
            }
        }
    }

    private fun displayResponse(text: String) {
        binding.tvOutput.text = text
    }

    private fun checkAndRequestAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            == PackageManager.PERMISSION_GRANTED
        ) {
            displayResponse("🎙️ Mic active! Ready for Phase 3 speech integration.\nTry typing commands (e.g. 'battery', 'open youtube', 'wifi settings').")
        } else {
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startOrbPulseAnimation() {
        val pulse = AlphaAnimation(0.2f, 0.6f).apply {
            duration = 1400
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        binding.orbOuterRing.startAnimation(pulse)
    }
}
