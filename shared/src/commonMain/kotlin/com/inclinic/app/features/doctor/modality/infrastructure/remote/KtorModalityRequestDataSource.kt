package com.inclinic.app.features.doctor.modality.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.modality.infrastructure.remote.dto.ModalityChangeRequestDto
import com.inclinic.app.features.doctor.modality.infrastructure.remote.dto.RespondModalityChangeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorModalityRequestDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : ModalityRequestDataSource {

    override suspend fun getRequest(id: String): Result<ModalityChangeRequestDto> = runCatching {
        client.get {
            url("$baseUrl/api/v1/doctor/modality-requests/$id")
        }.body<ApiEnvelope<ModalityChangeRequestDto>>().data ?: error("No data in modality request response")
    }

    override suspend fun respond(id: String, body: RespondModalityChangeDto): Result<ModalityChangeRequestDto> = runCatching {
        client.post {
            url("$baseUrl/api/v1/doctor/modality-requests/$id/respond")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<ModalityChangeRequestDto>>().data ?: error("No data in modality respond response")
    }
}
