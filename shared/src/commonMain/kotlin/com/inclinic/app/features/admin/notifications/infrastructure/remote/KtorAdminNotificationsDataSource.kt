package com.inclinic.app.features.admin.notifications.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode

class KtorAdminNotificationsDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : AdminNotificationsDataSource {

    override suspend fun list(limit: Int): Result<AdminNotificationsResponseDto> = runCatching {
        client.get {
            url("$baseUrl/api/notifications?limit=$limit")
        }.body<ApiEnvelope<AdminNotificationsResponseDto>>().data ?: AdminNotificationsResponseDto()
    }

    override suspend fun markRead(id: String): Result<Unit> = runCatching {
        val response = client.post {
            url("$baseUrl/api/notifications/$id/read")
        }
        if (response.status != HttpStatusCode.OK && response.status.value !in 200..299) {
            error("Mark read failed with status ${response.status.value}")
        }
    }

    override suspend fun markAllRead(): Result<Unit> = runCatching {
        val response = client.post {
            url("$baseUrl/api/notifications/read-all")
        }
        if (response.status != HttpStatusCode.OK && response.status.value !in 200..299) {
            error("Mark all read failed with status ${response.status.value}")
        }
    }

    override suspend fun delete(id: String): Result<Unit> = runCatching {
        val response = client.delete {
            url("$baseUrl/api/notifications/$id")
        }
        if (response.status != HttpStatusCode.OK && response.status.value !in 200..299) {
            error("Delete failed with status ${response.status.value}")
        }
    }
}
