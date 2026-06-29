package com.inclinic.app.features.doctor.negotiation.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.dto.PackageNegotiationDto
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.dto.RespondNegotiationDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorDoctorNegotiationDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorNegotiationDataSource {

    override suspend fun getNegotiation(id: String): Result<PackageNegotiationDto> = runCatching {
        client.get {
            url("$baseUrl/api/package-negotiations/$id")
        }.body<ApiEnvelope<PackageNegotiationDto>>().data ?: error("No data in negotiation response")
    }

    override suspend fun respondNegotiation(id: String, body: RespondNegotiationDto): Result<PackageNegotiationDto> = runCatching {
        client.post {
            url("$baseUrl/api/package-negotiations/$id/respond")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<PackageNegotiationDto>>().data ?: error("No data in respond response")
    }
}
