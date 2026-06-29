package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.AppNotification
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url

class KtorNotificationDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : NotificationDataSource {

    override suspend fun getNotifications(limit: Int): Result<List<AppNotification>> = runCatching {
        client.get {
            url("$baseUrl/api/notifications")
            parameter("limit", limit)
        }.body<ApiEnvelope<List<AppNotification>>>().data ?: emptyList()
    }

    override suspend fun markAllRead(): Result<Unit> = runCatching {
        client.post {
            url("$baseUrl/api/notifications/mark-all-read")
        }
        Unit
    }
}
