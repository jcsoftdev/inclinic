package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.ChatMessage

interface DoctorChatDataSource {
    suspend fun getMessages(appointmentId: String): Result<List<ChatMessage>>

    /**
     * Sends a message in the appointment thread.
     * [attachments] is a list of URLs already uploaded via /api/upload; may be empty.
     */
    suspend fun sendMessage(
        appointmentId: String,
        text: String,
        attachments: List<String> = emptyList(),
    ): Result<ChatMessage>
}
