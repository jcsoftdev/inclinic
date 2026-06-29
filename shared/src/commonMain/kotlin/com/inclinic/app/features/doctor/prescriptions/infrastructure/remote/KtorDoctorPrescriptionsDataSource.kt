package com.inclinic.app.features.doctor.prescriptions.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.PrescriptionDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.UpdatePrescriptionRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorDoctorPrescriptionsDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorPrescriptionsDataSource {

    /** GET /api/prescriptions/{id} */
    override suspend fun getPrescription(id: String): Result<PrescriptionDto> = runCatching {
        client.get {
            url("$baseUrl/api/prescriptions/$id")
        }.body<ApiEnvelope<PrescriptionDto>>().data ?: error("No data in prescription response")
    }

    /** PUT /api/prescriptions/{id} */
    override suspend fun updatePrescription(id: String, body: UpdatePrescriptionRequestDto): Result<PrescriptionDto> = runCatching {
        client.put {
            url("$baseUrl/api/prescriptions/$id")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<PrescriptionDto>>().data ?: error("No data in update prescription response")
    }
}
