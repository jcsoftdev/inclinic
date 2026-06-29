package com.inclinic.app.features.doctor.notifications.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode

class KtorDoctorNotificationsDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorNotificationsDataSource {

    override suspend fun list(limit: Int): Result<NotificationsResponseDto> = runCatching {
        client.get {
            url("$baseUrl/api/notifications?limit=$limit")
        }.body<ApiEnvelope<NotificationsResponseDto>>().data ?: NotificationsResponseDto()
    }

    override suspend fun markRead(id: String): Result<Unit> = runCatching {
        val response = client.put {
            url("$baseUrl/api/notifications/$id")
        }
        if (response.status != HttpStatusCode.OK && response.status.value !in 200..299) {
            error("Mark read failed with status ${response.status.value}")
        }
    }

    override suspend fun markAllRead(): Result<Unit> = runCatching {
        val response = client.post {
            url("$baseUrl/api/notifications/mark-all-read")
        }
        if (response.status != HttpStatusCode.OK && response.status.value !in 200..299) {
            error("Mark all read failed with status ${response.status.value}")
        }
    }
}
