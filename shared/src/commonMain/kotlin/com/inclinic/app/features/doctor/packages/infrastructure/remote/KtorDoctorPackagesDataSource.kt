package com.inclinic.app.features.doctor.packages.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.packages.infrastructure.remote.dto.CreatePackageRequestDto
import com.inclinic.app.features.doctor.packages.infrastructure.remote.dto.TherapyPackageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Talks to the real backend routes:
 * - GET    /api/therapy-packages           (doctor scope inferred from auth token)
 * - POST   /api/therapy-packages           (propose package to a patient)
 * - DELETE /api/therapy-packages/{id}       (cancel package)
 *
 * The list endpoint returns `{ success, data: [ ...packages ] }` — data is the
 * array directly. The create endpoint returns `{ success, data: { ...package } }`.
 */
class KtorDoctorPackagesDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorPackagesDataSource {

    override suspend fun list(): Result<List<TherapyPackageDto>> = runCatching {
        client.get {
            url("$baseUrl/api/therapy-packages")
        }.body<ApiEnvelope<List<TherapyPackageDto>>>().data ?: emptyList()
    }

    override suspend fun create(request: CreatePackageRequestDto): Result<TherapyPackageDto> = runCatching {
        client.post {
            url("$baseUrl/api/therapy-packages")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<ApiEnvelope<TherapyPackageDto>>().data ?: error("Create package response missing data")
    }

    override suspend fun cancel(id: String): Result<Unit> = runCatching {
        client.delete {
            url("$baseUrl/api/therapy-packages/$id")
        }
        Unit
    }
}
