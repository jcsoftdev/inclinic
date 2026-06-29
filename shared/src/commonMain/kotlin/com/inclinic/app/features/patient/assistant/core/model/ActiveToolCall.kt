package com.inclinic.app.features.patient.assistant.core.model

/**
 * Represents an in-flight tool call while the assistant is waiting for a result.
 * Used to display a [ToolLoadingPill] in the UI until [AssistantStreamEvent.ToolResult] arrives.
 */
data class ActiveToolCall(
    val toolCallId: String,
    val toolName: ToolName,
)
