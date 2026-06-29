package com.inclinic.app.features.patient.assistant.core.port

import com.inclinic.app.features.patient.assistant.core.model.AssistantChatRequest
import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import kotlinx.coroutines.flow.Flow

/**
 * Port (hexagonal boundary) for streaming assistant chat.
 *
 * The returned [Flow] emits:
 *   1. [AssistantStreamEvent.ConversationIdReceived] — always first (from `x-conversation-id` header)
 *   2. [AssistantStreamEvent.TextDelta] — incremental text chunks
 *   3. [AssistantStreamEvent.ToolCallStarted] / [AssistantStreamEvent.ToolResult] — interleaved tool events
 *   4. [AssistantStreamEvent.Finish] — stream closed normally
 *   5. [AssistantStreamEvent.Error] — HTTP or stream-level error (terminates the flow)
 *
 * Implemented by [com.inclinic.app.features.patient.assistant.infrastructure.KtorAssistantChatDataSource].
 * Faked in tests via a simple lambda or test double.
 */
interface AssistantChatDataSource {
    suspend fun sendMessage(request: AssistantChatRequest): Flow<AssistantStreamEvent>
}
