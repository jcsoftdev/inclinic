package com.inclinic.app.features.patient.assistant.presentation.component

import com.inclinic.app.features.patient.assistant.core.error.AssistantError
import com.inclinic.app.features.patient.assistant.core.model.ActiveToolCall
import com.inclinic.app.features.patient.assistant.core.model.AssistantMessage

/**
 * Immutable state for the assistant chat screen.
 *
 * [messages]          — finalized message list (User + Assistant + ToolResultCard)
 * [streamingBuffer]   — accumulated text from in-flight [AssistantStreamEvent.TextDelta] events;
 *                       flushed to [AssistantMessage.Assistant] on [AssistantStreamEvent.Finish]
 * [activeToolCall]    — non-null while a tool call is in flight (shows ToolLoadingPill)
 * [isStreaming]       — true while a stream is open; disables Send button
 * [inputText]         — current content of the chat input field
 * [error]             — non-null when a mapped error should be shown in the ErrorBanner
 * [retryAfterSeconds] — non-zero during a 429 cooldown; disables Send + shows countdown
 * [disclaimerVisible] — true by default, set to false after patient dismisses the banner
 * [conversationId]    — captured from `x-conversation-id` header; included in subsequent requests
 */
data class AssistantChatState(
    val messages: List<AssistantMessage> = emptyList(),
    val streamingBuffer: String = "",
    val activeToolCall: ActiveToolCall? = null,
    val isStreaming: Boolean = false,
    val inputText: String = "",
    val error: AssistantError? = null,
    val retryAfterSeconds: Int? = null,
    val disclaimerVisible: Boolean = true,
    val conversationId: String? = null,
)
