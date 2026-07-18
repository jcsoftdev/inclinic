package com.inclinic.app.features.doctor.prescriptions.infrastructure.remote

import com.inclinic.app.core.error.ApiResult
import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.core.network.runApi
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.CreatePrescriptionRequestDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.PrescriptionDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.UpdatePrescriptionRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
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
    override suspend fun getPrescription(id: String): Result<PrescriptionDto> = runApi {
        client.get {
            url("$baseUrl/api/prescriptions/$id")
        }.body<ApiEnvelope<PrescriptionDto>>().data ?: error("No data in prescription response")
    }.toResult()

    /** POST /api/prescriptions */
    override suspend fun createPrescription(body: CreatePrescriptionRequestDto): Result<PrescriptionDto> = runApi {
        client.post {
            url("$baseUrl/api/prescriptions")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<PrescriptionDto>>().data ?: error("No data in create prescription response")
    }.toResult()

    /** PUT /api/prescriptions/{id} */
    override suspend fun updatePrescription(id: String, body: UpdatePrescriptionRequestDto): Result<PrescriptionDto> = runApi {
        client.put {
            url("$baseUrl/api/prescriptions/$id")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<PrescriptionDto>>().data ?: error("No data in update prescription response")
    }.toResult()
}

/**
 * Bridges [ApiResult] (typed HTTP errors from [runApi]) to the [Result]-based
 * [DoctorPrescriptionsDataSource] contract. [com.inclinic.app.core.error.ApiError] extends
 * [Throwable], so callers up the chain (repository `.map`, `Throwable.toUserMessage()`) keep
 * working unchanged while gaining typed status handling — e.g. a 409 becomes
 * [com.inclinic.app.core.error.ApiError.Conflict] with the backend's own message instead of a
 * raw [io.ktor.client.plugins.ClientRequestException].
 */
private fun <T> ApiResult<T>.toResult(): Result<T> = when (this) {
    is ApiResult.Ok -> Result.success(value)
    is ApiResult.Err -> Result.failure(error)
}
