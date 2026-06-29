package com.inclinic.app.features.patient.assistant.infrastructure.dto

import kotlinx.serialization.Serializable

/**
 * Wire-format request body for POST /api/assistant/chat.
 *
 * [conversationId] is null on the first message; populated from the
 * `x-conversation-id` response header on subsequent messages.
 */
@Serializable
data class AssistantChatRequestDto(
    val message: String,
    val conversationId: String? = null,
)
