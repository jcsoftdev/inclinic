package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorDoctorProfileDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorProfileDataSource {

    override suspend fun getPriceConfig(doctorId: String): Result<DoctorPriceConfig> = runCatching {
        client.get {
            url("$baseUrl/api/doctors/$doctorId")
        }.body<ApiEnvelope<DoctorPriceConfig>>().data ?: error("Doctor not found")
    }

    override suspend fun updatePriceConfig(
        doctorId: String,
        fee: Double,
        supportsPresential: Boolean,
        supportsVirtual: Boolean,
    ): Result<DoctorPriceConfig> = runCatching {
        client.patch {
            url("$baseUrl/api/doctors/$doctorId")
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "consultationFee" to fee,
                    "supportsPresential" to supportsPresential,
                    "supportsVirtual" to supportsVirtual,
                )
            )
        }.body<ApiEnvelope<DoctorPriceConfig>>().data ?: error("Doctor update failed")
    }
}
