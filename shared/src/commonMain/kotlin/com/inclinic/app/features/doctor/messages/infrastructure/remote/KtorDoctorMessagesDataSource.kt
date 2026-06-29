package com.inclinic.app.features.doctor.messages.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url

class KtorDoctorMessagesDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorMessagesDataSource {

    override suspend fun listThreads(): Result<List<ChatThreadDto>> = runCatching {
        client.get {
            url("$baseUrl/api/chats")
        }.body<ApiEnvelope<List<ChatThreadDto>>>().data ?: emptyList()
    }
}
