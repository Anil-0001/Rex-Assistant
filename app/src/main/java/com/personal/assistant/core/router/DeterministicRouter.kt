package com.personal.assistant.core.router

import com.personal.assistant.core.actions.ActionResult
import com.personal.assistant.core.actions.AndroidActionExecutor

class DeterministicRouter(private val executor: AndroidActionExecutor) {

    fun execute(rawInput: String): ActionResult {
        var input = rawInput.trim().lowercase()

        // Strip "rex", "hey rex", "rex sun" prefix if user addresses by name
        val namePrefixes = listOf("hey rex ", "rex ", "rex sun ", "sun rex ")
        for (prefix in namePrefixes) {
            if (input.startsWith(prefix)) {
                input = input.removePrefix(prefix).trim()
                break
            }
        }

        // R.E.X. identity / who are you check
        if (input == "kaun ho" || input == "who are you" || input == "rex" || input == "status") {
            return ActionResult.Success(
                message = "Main hoon R.E.X. (Responsive Edge Executor) — aapka personal on-device AI assistant.",
                details = "System: All cores operational | Mode: Local Offline Priority"
            )
        }

        // Battery check
        if (input.contains("battery") || input.contains("charge") || input.contains("charging")) {
            return executor.getBatteryStatus()
        }

        // Wi-Fi / Internet
        if (input.contains("wifi") || input.contains("wi-fi") || input.contains("internet setting")) {
            return executor.openWifiSettings()
        }

        // Bluetooth
        if (input.contains("bluetooth")) {
            return executor.openBluetoothSettings()
        }

        // Time / Date
        if (input.contains("time") || input.contains("baje") || input.contains("date") || input.contains("tarikh")) {
            return executor.getCurrentTime()
        }

        // Device Info
        if (input.contains("device") || input.contains("specs") || input.contains("phone info") || input.contains("model")) {
            return executor.getDeviceInfo()
        }

        // App Launching (English + Hinglish patterns: "open X", "launch X", "X kholo", "X chalao", "start X")
        val openPrefixes = listOf("open ", "launch ", "start ", "run ")
        for (prefix in openPrefixes) {
            if (input.startsWith(prefix)) {
                val appName = input.removePrefix(prefix).trim()
                if (appName.isNotEmpty()) {
                    return executor.launchAppByName(appName)
                }
            }
        }

        val suffixPatterns = listOf(" kholo", " chalao", " open karo", " start karo")
        for (suffix in suffixPatterns) {
            if (input.endsWith(suffix)) {
                val appName = input.removeSuffix(suffix).trim()
                if (appName.isNotEmpty()) {
                    return executor.launchAppByName(appName)
                }
            }
        }

        return ActionResult.Error("Command not recognized offline. Ready for online reasoning engine in Phase 4.")
    }
}
