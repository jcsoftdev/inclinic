package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.core.model.Conversation
import com.inclinic.app.core.model.SenderRole
import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.patient.infrastructure.remote.dto.UploadResultDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorChatDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : ChatDataSource {

    override suspend fun getConversations(): Result<List<Conversation>> = runCatching {
        client.get {
            url("$baseUrl/api/chats")
        }.body<ApiEnvelope<List<Conversation>>>().data ?: emptyList()
    }

    override suspend fun getMessages(doctorId: String): Result<List<ChatMessage>> = runCatching {
        client.get {
            url("$baseUrl/api/chats/$doctorId")
        }.body<ApiEnvelope<ChatThreadDto>>().data?.messages?.map { it.toDomain() } ?: emptyList()
    }

    override suspend fun sendMessage(
        doctorId: String,
        content: String,
        attachments: List<String>,
    ): Result<ChatMessage> = runCatching {
        client.post {
            url("$baseUrl/api/chats/$doctorId")
            contentType(ContentType.Application.Json)
            setBody(
                JsonObject(
                    buildMap {
                        put("body", JsonPrimitive(content))
                        if (attachments.isNotEmpty()) {
                            put("attachments", JsonArray(attachments.map { JsonPrimitive(it) }))
                        }
                    },
                ),
            )
        }.body<ApiEnvelope<ChatMessageDto>>().data?.toDomain() ?: error("Message send failed")
    }

    override suspend fun uploadAttachment(
        bytes: ByteArray,
        fileName: String,
        mimeType: String,
    ): Result<UploadResultDto> = runCatching {
        client.post {
            url("$baseUrl/api/upload")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("bucket", CHAT_BUCKET)
                        append(
                            "file",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                                append(HttpHeaders.ContentType, mimeType)
                            },
                        )
                    },
                ),
            )
        }.body<ApiEnvelope<UploadResultDto>>().data ?: error("Upload failed")
    }

    private companion object {
        // Bucket privado para adjuntos clínicos (ver STORAGE_BUCKETS.MEDICAL_ATTACHMENTS en el backend).
        const val CHAT_BUCKET = "medical-attachments"
    }
}

/**
 * Hilo de chat party-based devuelto por GET /api/chats/{doctorId}.
 * Los mensajes vienen ascendentes por createdAt.
 */
@Serializable
internal data class ChatThreadDto(
    val threadId: String,
    val messages: List<ChatMessageDto> = emptyList(),
)

/**
 * Mensaje tal como lo emite el backend Hono. No incluye appointmentId ni senderId.
 */
@Serializable
internal data class ChatMessageDto(
    val id: String,
    val threadId: String,
    val senderRole: String,
    val body: String,
    val attachments: List<String> = emptyList(),
    val createdAt: String,
) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        senderRole = SenderRole.valueOf(senderRole),
        text = body,
        sentAt = runCatching { Instant.parse(createdAt) }.getOrElse { kotlin.time.Clock.System.now() },
        attachments = attachments,
    )
}
