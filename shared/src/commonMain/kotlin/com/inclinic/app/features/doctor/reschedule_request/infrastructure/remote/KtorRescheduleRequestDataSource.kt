package com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote.dto.CreateRescheduleRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorRescheduleRequestDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : RescheduleRequestDataSource {

    override suspend fun requestReschedule(
        appointmentId: String,
        body: CreateRescheduleRequestDto,
    ): Result<Appointment> = runCatching {
        client.post {
            url("$baseUrl/api/v1/doctor/appointments/$appointmentId/reschedule-request")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<Appointment>>().data ?: error("No data in reschedule request response")
    }
}
