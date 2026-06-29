package com.inclinic.app.features.patient.moderation.infrastructure.remote

import com.inclinic.app.features.patient.moderation.core.model.ReportCategory
import com.inclinic.app.features.patient.moderation.infrastructure.remote.dto.BlockUserRequestDto
import com.inclinic.app.features.patient.moderation.infrastructure.remote.dto.ReportUserRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/** Adapter: calls the authenticated Ktor client against /api/moderation endpoints. */
class KtorModerationRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : ModerationRemoteDataSource {

    override suspend fun reportUser(userId: String, reason: String, category: ReportCategory?): Result<Unit> =
        runCatching {
            val response = client.post {
                url("$baseUrl/api/moderation/report")
                contentType(ContentType.Application.Json)
                setBody(ReportUserRequestDto(userId = userId, reason = reason, category = category?.apiValue))
            }
            if (!response.status.isSuccess()) error("Error al enviar reporte (${response.status.value})")
        }

    override suspend fun blockUser(userId: String, reason: String?): Result<Unit> =
        runCatching {
            val response = client.post {
                url("$baseUrl/api/moderation/block")
                contentType(ContentType.Application.Json)
                setBody(BlockUserRequestDto(userId = userId, reason = reason?.ifBlank { null }))
            }
            if (!response.status.isSuccess()) error("Error al bloquear usuario (${response.status.value})")
        }

    override suspend fun unblockUser(userId: String): Result<Unit> =
        runCatching {
            val response = client.delete {
                url("$baseUrl/api/moderation/block/$userId")
            }
            if (!response.status.isSuccess()) error("Error al desbloquear usuario (${response.status.value})")
        }
}
