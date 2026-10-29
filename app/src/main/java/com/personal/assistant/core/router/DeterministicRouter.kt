package com.personal.assistant.core.router

import com.personal.assistant.core.actions.ActionResult
import com.personal.assistant.core.actions.AndroidActionExecutor

class DeterministicRouter(private val executor: AndroidActionExecutor) {

    fun execute(rawInput: String): ActionResult {
        val input = rawInput.trim().lowercase()

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
