package com.personal.assistant.core.brain

sealed class BrainDecision {
    data class ExecuteTool(
        val toolName: String,
        val argument: String? = null,
        val spokenReasoning: String? = null
    ) : BrainDecision()

    data class DirectReply(
        val replyText: String
    ) : BrainDecision()

    data class Error(
        val message: String
    ) : BrainDecision()
}

interface RexCognitiveBrain {
    suspend fun think(userPrompt: String, isOnline: Boolean): BrainDecision
}
