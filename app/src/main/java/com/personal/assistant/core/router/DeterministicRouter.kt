package com.personal.assistant.core.router

import com.personal.assistant.core.actions.ActionResult
import com.personal.assistant.core.actions.AndroidActionExecutor

class DeterministicRouter(private val executor: AndroidActionExecutor) {

    fun execute(rawInput: String): ActionResult {
        var input = rawInput.trim().lowercase()

        // Strip "rex", "hey rex", "rex sun", "sun rex" prefixes
        val namePrefixes = listOf("hey rex ", "rex ", "rex sun ", "sun rex ")
        for (prefix in namePrefixes) {
            if (input.startsWith(prefix)) {
                input = input.removePrefix(prefix).trim()
                break
            }
        }

        // R.E.X. identity / status query
        if (input == "kaun ho" || input == "who are you" || input == "rex" || input == "status") {
            return ActionResult.Success(
                message = "Main hoon R.E.X. (Responsive Edge Executor) — aapka personal on-device AI assistant.",
                details = "System: All cores operational | Mode: Local Offline Priority | Security: Tiered Enforced"
            )
        }

        // Battery telemetry
        if (input.contains("battery") || input.contains("charge") || input.contains("charging")) {
            return executor.getBatteryStatus()
        }

        // Storage telemetry (New Phase 2)
        if (input.contains("storage") || input.contains("space") || input.contains("memory bachi") || input.contains("kitna space")) {
            return executor.getStorageStatus()
        }

        // Network telemetry (New Phase 2)
        if (input.contains("network") || input.contains("internet") || input.contains("net chal raha") || input.contains("wifi status")) {
            return executor.getNetworkStatus()
        }

        // Wi-Fi / Internet Settings Panel
        if (input.contains("wifi panel") || input.contains("wi-fi setting") || input.contains("open wifi") || input.contains("wifi kholo")) {
            return executor.openWifiSettings()
        }

        // Bluetooth Settings
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

        // List installed apps count (New Phase 2)
        if (input.contains("installed apps") || input.contains("kitne app") || input.contains("all apps")) {
            val apps = executor.indexInstalledApps()
            return ActionResult.Success(
                message = "R.E.X. has indexed ${apps.size} installed apps on this device.",
                details = "Top indexed: " + apps.take(8).joinToString { it.label } + "..."
            )
        }

        // Purge / Clear Cache Test (Safety Confirmation Demo - New Phase 2)
        if (input.contains("clear cache") || input.contains("purge") || input.contains("delete data") || input.contains("reset cache")) {
            return executor.requestClearDataSimulation()
        }

        // Dynamic App Launching (English + Hinglish prefixes)
        val openPrefixes = listOf("open ", "launch ", "start ", "run ", "kholo ", "chalao ")
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

        // Direct single-word app match (e.g. user just types "camera", "calculator", "whatsapp")
        if (!input.contains(" ")) {
            val appMatch = executor.launchAppByName(input)
            if (appMatch is ActionResult.Success) {
                return appMatch
            }
        }

        return ActionResult.Error("R.E.X. does not recognize command offline. Online LLM reasoning activates in Phase 4.")
    }
}
