package com.inclinic.app.features.patient.assistant.core.model

import kotlinx.serialization.json.JsonElement

/**
 * Sealed hierarchy of events emitted from [com.inclinic.app.features.patient.assistant.core.port.AssistantChatDataSource].
 *
 * Wire format: Vercel AI SDK v6 UIMessageStream (`data: {json}\n\n` lines).
 *
 * Key field name corrections vs. the design draft (verified from AI SDK v6 type defs):
 * - `text-delta` chunk has field `delta` (not `textDelta`)
 * - tool call chunk type is `tool-input-available` (not `tool-call`), field `input` (not `args`)
 * - tool result chunk type is `tool-output-available` (not `tool-result`), field `output` (not `result`)
 * - error chunk has field `errorText` (not `error`)
 *
 * Envelope-only chunks (text-start, text-end, start, start-step, finish-step, abort) are silently skipped.
 */
sealed class AssistantStreamEvent {

    /** `x-conversation-id` header value, emitted before the body channel is drained. */
    data class ConversationIdReceived(val conversationId: String) : AssistantStreamEvent()

    /** Incremental text from the assistant; accumulate into the streaming buffer. */
    data class TextDelta(val text: String) : AssistantStreamEvent()

    /**
     * The LLM has invoked a tool. Corresponds to `tool-input-available` chunk.
     * [args] is the raw [JsonElement] from the `input` field — typed deserialization
     * is deferred to UI-layer consumers.
     */
    data class ToolCallStarted(
        val toolCallId: String,
        val toolName: ToolName,
        val args: JsonElement,
    ) : AssistantStreamEvent()

    /**
     * A tool has returned its result. Corresponds to `tool-output-available` chunk.
     * [result] is the raw [JsonElement] from the `output` field.
     */
    data class ToolResult(
        val toolCallId: String,
        val toolName: ToolName,
        val result: JsonElement,
    ) : AssistantStreamEvent()

    /** Stream has finished normally (`finish` chunk with `finishReason`). */
    data object Finish : AssistantStreamEvent()

    /** A stream-level error or HTTP error mapped to domain. */
    data class Error(
        val code: String,
        val retryAfterSeconds: Int? = null,
    ) : AssistantStreamEvent()
}
