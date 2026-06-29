package com.inclinic.app.features.doctor.reschedule.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RescheduleRequestDto
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RespondRescheduleRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorRescheduleQueueDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : RescheduleQueueDataSource {

    override suspend fun listRequests(): Result<List<RescheduleRequestDto>> = runCatching {
        client.get {
            url("$baseUrl/api/v1/doctor/me/reschedule-requests")
        }.body<ApiEnvelope<List<RescheduleRequestDto>>>().data ?: emptyList()
    }

    override suspend fun respond(id: String, body: RespondRescheduleRequestDto): Result<RescheduleRequestDto> = runCatching {
        client.post {
            url("$baseUrl/api/v1/doctor/reschedule-requests/$id/respond")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<RescheduleRequestDto>>().data ?: error("No data in respond response")
    }
}
