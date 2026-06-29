package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.ChatMessage
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

class KtorDoctorChatDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorChatDataSource {

    override suspend fun getMessages(appointmentId: String): Result<List<ChatMessage>> = runCatching {
        client.get {
            url("$baseUrl/api/chats/$appointmentId/messages")
        }.body<ApiEnvelope<List<ChatMessage>>>().data ?: emptyList()
    }

    override suspend fun sendMessage(
        appointmentId: String,
        text: String,
        attachments: List<String>,
    ): Result<ChatMessage> = runCatching {
        client.post {
            url("$baseUrl/api/chats/$appointmentId/messages")
            contentType(ContentType.Application.Json)
            setBody(
                JsonObject(
                    buildMap {
                        put("text", JsonPrimitive(text))
                        if (attachments.isNotEmpty()) {
                            put("attachments", JsonArray(attachments.map { JsonPrimitive(it) }))
                        }
                    },
                ),
            )
        }.body<ApiEnvelope<ChatMessage>>().data ?: error("Send message failed")
    }
}
