package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class KtorShareDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : ShareDataSource {

    override suspend fun getShareRequests(): Result<List<ShareRequest>> = runCatching {
        client.get {
            url("$baseUrl/api/medical-history-share")
        }.body<ApiEnvelope<List<ShareRequest>>>().data ?: emptyList()
    }

    override suspend fun getShareRequestDetail(requestId: String): Result<ShareRequest> = runCatching {
        client.get {
            url("$baseUrl/api/medical-history-share/$requestId")
        }.body<ApiEnvelope<ShareRequest>>().data!!
    }

    override suspend fun respondToShareRequest(
        requestId: String,
        action: String,
        duration: Int?,
    ): Result<ShareRequest> = runCatching {
        client.patch {
            url("$baseUrl/api/medical-history-share/$requestId")
            contentType(ContentType.Application.Json)
            setBody(ShareResponseBody(action = action, duration = duration))
        }.body<ApiEnvelope<ShareRequest>>().data!!
    }

    override suspend fun revokeAccess(requestId: String): Result<ShareRequest> = runCatching {
        client.delete {
            url("$baseUrl/api/medical-history-share/$requestId")
        }.body<ApiEnvelope<ShareRequest>>().data!!
    }
}

@Serializable
private data class ShareResponseBody(
    val action: String,
    val duration: Int? = null,
)
