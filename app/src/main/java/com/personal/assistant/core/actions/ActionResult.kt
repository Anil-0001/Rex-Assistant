package com.personal.assistant.core.actions

sealed class ActionResult {
    data class Success(val message: String, val details: String? = null) : ActionResult()
    data class RequiresConfirmation(val prompt: String, val actionId: String) : ActionResult()
    data class Error(val reason: String) : ActionResult()
}

enum class ActionSafetyTier {
    SAFE_READ,       // Battery, clock, device info (Executes directly)
    EXTERNAL_LAUNCH, // Opening apps, opening settings (Executes with notice)
    MUTATING         // Modifying state, clearing data, sending alerts (Requires prompt)
}
