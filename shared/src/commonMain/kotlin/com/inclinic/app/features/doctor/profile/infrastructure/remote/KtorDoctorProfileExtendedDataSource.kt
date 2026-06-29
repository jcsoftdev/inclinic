package com.inclinic.app.features.doctor.profile.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

// DTOs

@Serializable
data class DoctorProfileDto(
    val id: String,
    val bio: String? = null,
    val licenseNumber: String? = null,
    val consultationPrice: Double = 0.0,
    val offersHomeVisit: Boolean = false,
    val offersTelemedicine: Boolean = false,
    val avatar: String? = null,
    val photos: List<String> = emptyList(),
    val user: DoctorUserDto? = null,
    val specialties: List<DoctorSpecialtyDto> = emptyList(),
)

@Serializable
data class DoctorUserDto(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String? = null,
)

@Serializable
data class SpecialtyDto(val id: String, val name: String, val slug: String = "")

@Serializable
data class DoctorSpecialtyDto(
    val specialtyId: String = "",
    val specialty: SpecialtyDto? = null,
)

@Serializable
data class UpdateProfileRequestDto(
    val bio: String? = null,
    val consultationPrice: Double? = null,
    val offersHomeVisit: Boolean? = null,
    val offersTelemedicine: Boolean? = null,
    val licenseNumber: String? = null,
)

@Serializable
data class EditSpecialtiesRequestDto(val specialtyIds: List<String>)

/**
 * Income summary from GET /api/doctors/me/metrics (monthRevenue section).
 * The backend does NOT provide time-series bars; only monthly aggregates.
 */
@Serializable
data class DoctorMetricsDto(
    val monthRevenue: MonthRevenueDto? = null,
)

@Serializable
data class MonthRevenueDto(
    val amount: Double = 0.0,
    val commission: Double = 0.0,
    val net: Double = 0.0,
    val sessions: Int = 0,
    val growthPct: Double? = null,
)

@Serializable
data class DoctorReviewsDto(
    val averageRating: Double = 0.0,
    val totalRatings: Int = 0,
    val reviews: List<ReviewItemDto> = emptyList(),
)

@Serializable
data class ReviewItemDto(
    val id: String,
    val rating: Int? = null,
    val comment: String? = null,
    val date: String? = null,
    val specialty: String? = null,
    val patientInitials: String = "",
)

// DataSource interface

interface DoctorProfileExtendedDataSource {
    suspend fun getProfile(doctorId: String): Result<DoctorProfileDto>
    suspend fun updateProfile(doctorId: String, request: UpdateProfileRequestDto): Result<DoctorProfileDto>
    suspend fun editSpecialties(doctorId: String, specialtyIds: List<String>): Result<Unit>
    suspend fun getMetrics(): Result<DoctorMetricsDto>
    suspend fun getReviews(doctorId: String, limit: Int): Result<DoctorReviewsDto>
}

// Ktor implementation
//
// Real backend routes (ClinicAI Next.js App Router):
//   GET  /api/doctors/{doctorId}              - public profile
//   PUT  /api/doctors/{doctorId}              - update profile
//   PUT  /api/doctors/{doctorId}/specialties  - update specialties
//   GET  /api/doctors/me/metrics              - income/metrics (month-level, no bars)
//   GET  /api/doctors/{doctorId}/reviews      - public reviews
//
// Previously called /api/v1/doctor/me/* routes that do NOT exist on the backend.
// The specialty-requests endpoints also don't exist on the backend.

class KtorDoctorProfileExtendedDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorProfileExtendedDataSource {

    override suspend fun getProfile(doctorId: String): Result<DoctorProfileDto> = runCatching {
        client.get {
            url("$baseUrl/api/doctors/$doctorId")
        }.body<ApiEnvelope<DoctorProfileDto>>().data ?: error("Profile data missing")
    }

    override suspend fun updateProfile(doctorId: String, request: UpdateProfileRequestDto): Result<DoctorProfileDto> =
        runCatching {
            client.put {
                url("$baseUrl/api/doctors/$doctorId")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body<ApiEnvelope<DoctorProfileDto>>().data ?: error("Update profile response missing data")
        }

    override suspend fun editSpecialties(doctorId: String, specialtyIds: List<String>): Result<Unit> = runCatching {
        val response = client.put {
            url("$baseUrl/api/doctors/$doctorId/specialties")
            contentType(ContentType.Application.Json)
            setBody(EditSpecialtiesRequestDto(specialtyIds))
        }
        if (response.status.value !in 200..299) {
            error("Edit specialties failed with status ${response.status.value}")
        }
    }

    override suspend fun getMetrics(): Result<DoctorMetricsDto> = runCatching {
        client.get {
            url("$baseUrl/api/doctors/me/metrics")
        }.body<ApiEnvelope<DoctorMetricsDto>>().data ?: error("Metrics data missing")
    }

    override suspend fun getReviews(doctorId: String, limit: Int): Result<DoctorReviewsDto> = runCatching {
        client.get {
            url("$baseUrl/api/doctors/$doctorId/reviews?limit=$limit")
        }.body<ApiEnvelope<DoctorReviewsDto>>().data ?: error("Reviews data missing")
    }
}
