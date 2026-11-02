package com.personal.assistant.core.brain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Zero-cost Cloud AI Brain utilizing Google AI Studio's Gemini 1.5 Flash Free Tier.
 * Native Java HttpURLConnection & JSONObject - Zero external dependencies.
 */
class GeminiCloudBrain(private var apiKey: String = "") : RexCognitiveBrain {

    fun setApiKey(key: String) {
        this.apiKey = key.trim()
    }

    fun hasValidKey(): Boolean = apiKey.length >= 20

    override suspend fun think(userPrompt: String, isOnline: Boolean): BrainDecision {
        if (!isOnline) {
            return BrainDecision.Error("OFFLINE_FALLBACK")
        }

        if (!hasValidKey()) {
            return BrainDecision.Error("NO_API_KEY")
        }

        return withContext(Dispatchers.IO) {
            try {
                callGemini(userPrompt)
            } catch (e: Exception) {
                BrainDecision.Error("Brain communication error: ${e.localizedMessage ?: "Unknown"}")
            }
        }
    }

    private fun callGemini(prompt: String): BrainDecision {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val url = URL(endpoint)
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        conn.doOutput = true
        conn.connectTimeout = 15000
        conn.readTimeout = 20000

        val requestBody = buildRequestBody(prompt)

        OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
            os.write(requestBody.toString())
            os.flush()
        }

        val responseCode = conn.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val responseText = conn.inputStream.bufferedReader().use(BufferedReader::readText)
            return parseGeminiResponse(responseText)
        } else {
            val errorRaw = conn.errorStream?.bufferedReader()?.use(BufferedReader::readText) ?: "Code: $responseCode"
            val errorClean = try {
                val errObj = JSONObject(errorRaw)
                errObj.optJSONObject("error")?.optString("message") ?: errorRaw
            } catch (e: Exception) {
                errorRaw
            }
            return BrainDecision.Error("Gemini ($responseCode): $errorClean")
        }
    }

    private fun buildRequestBody(userPrompt: String): JSONObject {
        val root = JSONObject()

        // 1. System Instruction - Defines R.E.X. Persona & Tool Rules
        val systemInstruction = JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().apply {
                    put("text", """
                        You are R.E.X. (Responsive Edge Executor), a sophisticated on-device AI assistant inspired directly by J.A.R.V.I.S. from the Iron Man universe.
                        You address the user respectfully as "boss" or "sir".
                        Your voice output is spoken aloud through text-to-speech, so respond naturally, intelligently, and concisely in friendly Hinglish or English.
                        
                        You have real device execution tools for Android:
                        - getBatteryStatus: Check phone battery percentage and charging status.
                        - getStorageStatus: Check internal storage free GB and capacity.
                        - getNetworkStatus: Check Wi-Fi, 5G cellular connection, and speed.
                        - openWifiSettings: Open phone Internet/Wi-Fi connection panel.
                        - openBluetoothSettings: Open phone Bluetooth settings.
                        - getCurrentTime: Check current time, day, and date.
                        - getDeviceInfo: Get phone model, hardware specs, and Android OS version.
                        - launchAppByName: Launch any installed phone app (YouTube, Camera, WhatsApp, Calculator, Settings, Chrome, etc.). Parameter: 'query'.
                        - requestClearDataSimulation: Reset cache/logs requiring user confirmation.
                        
                        CRITICAL INSTRUCTIONS:
                        1. If the user asks about the world, top news, facts, stories, calculations, coding, life advice, or jokes (e.g. "aaj top 5 news batao" or "who is Elon Musk"), PROVIDE THE DIRECT ANSWER IMMEDIATELY as natural spoken text. Do not call a tool unless an on-device phone action is genuinely needed.
                        2. If the user wants to launch an app, check battery, storage, network, or device settings, execute the corresponding tool function.
                    """.trimIndent())
                })
            })
        }
        root.put("systemInstruction", systemInstruction)

        // 2. Tools Declarations (Gemini Function Calling Schema)
        val toolsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("functionDeclarations", buildFunctionDeclarations())
            })
        }
        root.put("tools", toolsArray)

        // 3. User Prompt Contents
        val contents = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", userPrompt)
                    })
                })
            })
        }
        root.put("contents", contents)

        return root
    }

    private fun buildFunctionDeclarations(): JSONArray {
        val funcs = JSONArray()

        funcs.put(createSimpleFunc("getBatteryStatus", "Get the real-time battery level and charging state of the phone."))
        funcs.put(createSimpleFunc("getStorageStatus", "Check the phone's internal storage space, available GB, and total capacity."))
        funcs.put(createSimpleFunc("getNetworkStatus", "Inspect current network connectivity (Wi-Fi, 5G Cellular) and internet speed."))
        funcs.put(createSimpleFunc("openWifiSettings", "Open Android 14 Internet/Wi-Fi connection panel."))
        funcs.put(createSimpleFunc("openBluetoothSettings", "Open device Bluetooth settings screen."))
        funcs.put(createSimpleFunc("getCurrentTime", "Get current time, day, and date."))
        funcs.put(createSimpleFunc("getDeviceInfo", "Get device model, processor, and Android OS version."))
        funcs.put(createSimpleFunc("requestClearDataSimulation", "Request sensitive cache reset requiring user confirmation."))

        // launchAppByName with argument
        val launchApp = JSONObject().apply {
            put("name", "launchAppByName")
            put("description", "Launch any installed application on the phone (e.g. youtube, camera, whatsapp, calculator, settings, etc.)")
            put("parameters", JSONObject().apply {
                put("type", "OBJECT")
                put("properties", JSONObject().apply {
                    put("query", JSONObject().apply {
                        put("type", "STRING")
                        put("description", "The name of the app to launch (e.g., 'camera', 'youtube', 'calculator', 'whatsapp').")
                    })
                })
                put("required", JSONArray().apply { put("query") })
            })
        }
        funcs.put(launchApp)

        return funcs
    }

    private fun createSimpleFunc(name: String, desc: String): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("description", desc)
        }
    }

    private fun parseGeminiResponse(jsonText: String): BrainDecision {
        val root = JSONObject(jsonText)
        val candidates = root.optJSONArray("candidates") ?: return BrainDecision.Error("No candidates returned from Gemini.")

        if (candidates.length() == 0) return BrainDecision.Error("Empty response from AI Brain.")

        val firstCandidate = candidates.getJSONObject(0)
        val content = firstCandidate.optJSONObject("content") ?: return BrainDecision.Error("No content in candidate.")
        val parts = content.optJSONArray("parts") ?: return BrainDecision.Error("No parts in candidate.")

        for (i in 0 until parts.length()) {
            val part = parts.getJSONObject(i)

            // 1. Tool Call emitted by model
            if (part.has("functionCall")) {
                val funcCall = part.getJSONObject("functionCall")
                val toolName = funcCall.getString("name")
                val argsObj = funcCall.optJSONObject("args")
                val queryArg = if (argsObj != null && argsObj.has("query")) argsObj.getString("query") else null

                return BrainDecision.ExecuteTool(
                    toolName = toolName,
                    argument = queryArg,
                    spokenReasoning = null
                )
            }

            // 2. Direct conversational answer emitted by model
            if (part.has("text")) {
                val reply = part.getString("text").trim()
                if (reply.isNotEmpty()) {
                    return BrainDecision.DirectReply(reply)
                }
            }
        }

        return BrainDecision.Error("AI Brain did not produce executable decision.")
    }
}
