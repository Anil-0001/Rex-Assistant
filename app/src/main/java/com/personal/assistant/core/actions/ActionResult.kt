package com.personal.assistant.core.actions

sealed class ActionResult {
    data class Success(val message: String, val details: String? = null) : ActionResult()
    data class RequiresConfirmation(
        val title: String,
        val prompt: String,
        val actionId: String,
        val onConfirm: () -> ActionResult
    ) : ActionResult()
    data class Error(val reason: String) : ActionResult()
}

enum class ActionSafetyTier {
    SAFE_READ,       // Battery, clock, storage, device info (Executes directly)
    EXTERNAL_LAUNCH, // Opening apps, opening settings (Executes with notice)
    MUTATING         // Modifying state, clearing data, toggling critical controls (Requires prompt)
}
