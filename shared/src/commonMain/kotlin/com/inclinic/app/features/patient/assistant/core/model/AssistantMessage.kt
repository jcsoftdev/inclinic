package com.inclinic.app.features.patient.assistant.core.model

import kotlinx.serialization.json.JsonElement

/**
 * Sealed hierarchy for messages rendered in the assistant chat thread.
 *
 * - [User] — patient-authored message (right-aligned bubble)
 * - [Assistant] — LLM text response, finalized on [AssistantStreamEvent.Finish]
 * - [ToolResultCard] — inline card rendered when a tool result is processed
 */
sealed class AssistantMessage {
    abstract val id: String

    data class User(
        override val id: String,
        val text: String,
    ) : AssistantMessage()

    data class Assistant(
        override val id: String,
        val text: String,
    ) : AssistantMessage()

    data class ToolResultCard(
        override val id: String,
        val toolCallId: String,
        val toolName: ToolName,
        val result: JsonElement,
    ) : AssistantMessage()
}
