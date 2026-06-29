package com.inclinic.app.features.doctor.messages.infrastructure.remote

import kotlinx.serialization.Serializable

interface DoctorMessagesDataSource {
    /** GET /api/chats — lists chat threads for the current user. */
    suspend fun listThreads(): Result<List<ChatThreadDto>>
}

/**
 * Matches the response from listChatThreads in chat.service.ts:
 * { id, otherPartyId, otherPartyName, lastMessage, lastAt, unread }
 */
@Serializable
data class ChatThreadDto(
    val id: String,
    val otherPartyId: String,
    val otherPartyName: String,
    val lastMessage: String? = null,
    val lastAt: String,
    val unread: Boolean = false,
)
