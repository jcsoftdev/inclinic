package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.core.model.Conversation
import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto

interface ChatDataSource {
    suspend fun getConversations(): Result<List<Conversation>>
    suspend fun getMessages(doctorId: String): Result<List<ChatMessage>>
    suspend fun sendMessage(
        doctorId: String,
        content: String,
        attachments: List<String> = emptyList(),
    ): Result<ChatMessage>

    /** Sube un archivo (imagen / PDF) al bucket de adjuntos clínicos y devuelve su URL. */
    suspend fun uploadAttachment(bytes: ByteArray, fileName: String, mimeType: String): Result<UploadResultDto>
}
