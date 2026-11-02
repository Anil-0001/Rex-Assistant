package com.personal.assistant.core.router

import com.personal.assistant.core.actions.ActionResult
import com.personal.assistant.core.actions.AndroidActionExecutor
import com.personal.assistant.core.brain.BrainDecision
import com.personal.assistant.core.brain.GeminiCloudBrain

/**
 * Hybrid Orchestrator:
 * Manages the transition between Offline Deterministic Router and Cloud Cognitive AI Brain.
 */
class HybridOrchestrator(
    private val executor: AndroidActionExecutor,
    private val deterministicRouter: DeterministicRouter,
    val cloudBrain: GeminiCloudBrain
) {

    suspend fun process(input: String, isOnline: Boolean): ActionResult {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return ActionResult.Error("Empty query received.")
        }

        // Fast-path: If user has not configured API key or device is offline, route to local Deterministic Router immediately
        if (!isOnline || !cloudBrain.hasValidKey()) {
            return deterministicRouter.execute(trimmed)
        }

        // Online & Brain Ready -> Invoke Gemini 1.5 Flash Cognitive Layer
        val decision = cloudBrain.think(trimmed, isOnline)

        return when (decision) {
            is BrainDecision.ExecuteTool -> {
                executeToolDecision(decision.toolName, decision.argument)
            }
            is BrainDecision.DirectReply -> {
                ActionResult.Success(
                    message = decision.replyText,
                    details = "Cognitive Source: Gemini 1.5 Flash (Cloud Brain)"
                )
            }
            is BrainDecision.Error -> {
                // If Cloud fails or is unreachable, seamlessly fall back to local offline router!
                val fallback = deterministicRouter.execute(trimmed)
                if (fallback !is ActionResult.Error) {
                    fallback
                } else {
                    ActionResult.Error("${decision.message} (Offline fallback also unresolved)")
                }
            }
        }
    }

    private fun executeToolDecision(toolName: String, argument: String?): ActionResult {
        return when (toolName) {
            "getBatteryStatus" -> executor.getBatteryStatus()
            "getStorageStatus" -> executor.getStorageStatus()
            "getNetworkStatus" -> executor.getNetworkStatus()
            "openWifiSettings" -> executor.openWifiSettings()
            "openBluetoothSettings" -> executor.openBluetoothSettings()
            "getCurrentTime" -> executor.getCurrentTime()
            "getDeviceInfo" -> executor.getDeviceInfo()
            "launchAppByName" -> {
                if (!argument.isNullOrBlank()) {
                    executor.launchAppByName(argument)
                } else {
                    ActionResult.Error("Brain called launchApp without app name.")
                }
            }
            "requestClearDataSimulation" -> executor.requestClearDataSimulation()
            else -> ActionResult.Error("Unknown tool '$toolName' requested by AI Brain.")
        }
    }
}
