package com.inclinic.app.features.patient.assistant.core.model

/**
 * Input to [com.inclinic.app.features.patient.assistant.core.port.AssistantChatDataSource.sendMessage].
 *
 * [conversationId] is null on the first message of a session.
 * Subsequent messages must include the value captured from the `x-conversation-id` response header.
 */
data class AssistantChatRequest(
    val message: String,
    val conversationId: String? = null,
)
