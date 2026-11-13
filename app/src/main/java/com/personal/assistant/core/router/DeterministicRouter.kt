package com.personal.assistant.core.router

import com.personal.assistant.core.actions.ActionResult
import com.personal.assistant.core.actions.AndroidActionExecutor

class DeterministicRouter(private val executor: AndroidActionExecutor) {

    fun execute(rawInput: String): ActionResult {
        var input = rawInput.trim().lowercase()

        // Strip "rex", "hey rex", "rex sun", "sun rex", and Hindi prefixes
        val namePrefixes = listOf(
            "hey rex ", "rex ", "rex sun ", "sun rex ",
            "हे रेक्स ", "रेक्स सुनो ", "रेक्स ", "सुनो रेक्स "
        )
        for (prefix in namePrefixes) {
            if (input.startsWith(prefix)) {
                input = input.removePrefix(prefix).trim()
                break
            }
        }

        // R.E.X. identity / status query
        if (input == "kaun ho" || input == "who are you" || input == "rex" || input == "status" || input == "तुम कौन हो") {
            return ActionResult.Success(
                message = "Main hoon R.E.X. (Responsive Edge Executor) — aapka personal on-device AI assistant.",
                details = "System: All cores operational | Mode: Local Offline Priority | Security: Tiered Enforced"
            )
        }

        // Common conversational greetings (Offline resilience)
        if (input == "hello" || input == "hi" || input == "hey" || input == "namaste" ||
            input == "नमस्ते" || input == "हेलो" || input == "नमस्कार") {
            return ActionResult.Success(
                message = "Namaste boss! R.E.X. ready hai. Kaise madad karun?",
                details = "Mode: Offline Greeting Handler"
            )
        }

        if (input.contains("kaise ho") || input.contains("how are you") || input.contains("kya haal") ||
            input.contains("हाल चाल") || input.contains("कैसे हो")) {
            return ActionResult.Success(
                message = "Main bilkul badhiya hoon boss! All systems operational.",
                details = "Health: 100% | Mode: Offline Operational"
            )
        }

        if (input.contains("thank you") || input.contains("thanks") || input.contains("shukriya") ||
            input.contains("dhanyawad") || input.contains("धन्यवाद") || input.contains("शुक्रिया")) {
            return ActionResult.Success(
                message = "Aapka swagat hai boss! Always at your service.",
                details = "Mode: Polite Feedback"
            )
        }

        if (input == "bye" || input == "goodbye" || input == "alvida" || input == "अलविदा" || input == "बाय") {
            return ActionResult.Success(
                message = "Alvida boss! Have a great time ahead.",
                details = "Mode: Standby"
            )
        }

        // Battery telemetry
        if (input.contains("battery") || input.contains("charge") || input.contains("charging") ||
            input.contains("चार्ज") || input.contains("बैटरी")) {
            return executor.getBatteryStatus()
        }

        // Storage telemetry
        if (input.contains("storage") || input.contains("space") || input.contains("memory") ||
            input.contains("jagah") || input.contains("kitna space") || input.contains("स्टोरेज") || input.contains("मेमोरी")) {
            return executor.getStorageStatus()
        }

        // Network telemetry
        if (input.contains("network") || input.contains("internet") || input.contains("net chal") ||
            input.contains("wifi status") || input.contains("इंटरनेट") || input.contains("नेटवर्क")) {
            return executor.getNetworkStatus()
        }

        // Wi-Fi / Internet Settings Panel
        if (input.contains("wifi panel") || input.contains("wi-fi setting") || input.contains("open wifi") ||
            input.contains("wifi kholo") || input.contains("वाइफ़ाई")) {
            return executor.openWifiSettings()
        }

        // Bluetooth Settings
        if (input.contains("bluetooth") || input.contains("ब्लूटूथ")) {
            return executor.openBluetoothSettings()
        }

        // Time / Date
        if (input.contains("time") || input.contains("baje") || input.contains("date") ||
            input.contains("tarikh") || input.contains("समय") || input.contains("तारीख")) {
            return executor.getCurrentTime()
        }

        // Device Info
        if (input.contains("device") || input.contains("specs") || input.contains("phone info") || input.contains("model")) {
            return executor.getDeviceInfo()
        }

        // List installed apps count
        if (input.contains("installed apps") || input.contains("kitne app") || input.contains("all apps") || input.contains("कुल ऐप")) {
            val apps = executor.indexInstalledApps()
            return ActionResult.Success(
                message = "R.E.X. has indexed ${apps.size} installed apps on this device.",
                details = "Top indexed: " + apps.take(8).joinToString { it.label } + "..."
            )
        }

        // Purge / Clear Cache Test
        if (input.contains("clear cache") || input.contains("purge") || input.contains("delete data") || input.contains("reset cache")) {
            return executor.requestClearDataSimulation()
        }

        // --- DYNAMIC APP LAUNCHING (Natural Hindi + Hinglish + English) ---
        // Clean common trigger/action verbs from voice
        val openPatterns = listOf(
            "open ", "launch ", "start ", "run ", "kholo ", "chalao ", "khol de ", "chala de ",
            "khol do ", "chala do ", "khol ", "open kar ", "open karo ", "open kar do ",
            "खोलो ", "खोल दो ", "चलाओ ", "शुरू करो ", "ओपन करो "
        )

        for (pattern in openPatterns) {
            if (input.startsWith(pattern)) {
                val appName = input.removePrefix(pattern).trim()
                if (appName.isNotEmpty()) {
                    return executor.launchAppByName(appName)
                }
            }
        }

        val suffixPatterns = listOf(
            " kholo", " chalao", " khol de", " khol do", " chala de", " chala do", " khol",
            " open", " open karo", " open kar do", " start karo", " launch karo",
            " खोलो", " खोल दो", " चलाओ", " चला दो", " ओपन करो"
        )

        for (suffix in suffixPatterns) {
            if (input.endsWith(suffix)) {
                val appName = input.removeSuffix(suffix).trim()
                if (appName.isNotEmpty()) {
                    return executor.launchAppByName(appName)
                }
            }
        }

        // If user says "whatsapp app", "youtube app" etc.
        if (input.endsWith(" app") || input.endsWith(" ऐप")) {
            val appName = input.removeSuffix(" app").removeSuffix(" ऐप").trim()
            if (appName.isNotEmpty()) {
                val directMatch = executor.launchAppByName(appName)
                if (directMatch is ActionResult.Success) return directMatch
            }
        }

        // Direct single/double word app search across all installed apps
        val directMatch = executor.launchAppByName(input)
        if (directMatch is ActionResult.Success) {
            return directMatch
        }

        return ActionResult.Error("R.E.X. could not process: \"$rawInput\". In Phase 4, cloud LLM reasoning handles free-form talk!")
    }
}
