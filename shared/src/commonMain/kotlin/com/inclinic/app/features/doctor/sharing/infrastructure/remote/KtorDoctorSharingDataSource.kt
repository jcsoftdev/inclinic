package com.inclinic.app.features.doctor.sharing.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.CreateShareRequestDto
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.ShareRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorDoctorSharingDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorSharingDataSource {

    override suspend fun listRequests(): Result<List<ShareRequestDto>> = runCatching {
        client.get {
            url("$baseUrl/api/medical-history-share")
        }.body<ApiEnvelope<List<ShareRequestDto>>>().data ?: emptyList()
    }

    override suspend fun requestShare(body: CreateShareRequestDto): Result<ShareRequestDto> = runCatching {
        client.post {
            url("$baseUrl/api/medical-history-share")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<ShareRequestDto>>().data ?: error("No data in share request response")
    }

    override suspend fun cancelRequest(id: String): Result<Unit> = runCatching {
        client.delete {
            url("$baseUrl/api/medical-history-share/$id")
        }
        Unit
    }
}
