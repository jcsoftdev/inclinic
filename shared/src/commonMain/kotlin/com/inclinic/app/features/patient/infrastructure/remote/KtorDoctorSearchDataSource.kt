package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.DoctorPlan
import com.inclinic.app.core.model.Review
import com.inclinic.app.core.model.Specialty
import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.core.network.PagedApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.serialization.Serializable

@Serializable
private data class DoctorUserDto(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
)

@Serializable
private data class DoctorSpecialtyWrapperDto(
    val specialty: DoctorSpecialtyDetailDto? = null,
)

@Serializable
private data class DoctorSpecialtyDetailDto(
    val id: String,
    val name: String,
)

@Serializable
private data class DoctorDto(
    val id: String,
    val user: DoctorUserDto = DoctorUserDto(),
    val avatar: String? = null,
    val specialties: List<DoctorSpecialtyWrapperDto> = emptyList(),
    val bio: String? = null,
    val licenseNumber: String? = null,
    val consultationPrice: Double = 0.0,
    val offersHomeVisit: Boolean = false,
    val offersTelemedicine: Boolean = false,
    val isActive: Boolean = false,
    val ratingAvg: Double? = null,
    val ratingCount: Int = 0,
) {
    fun toDomain() = Doctor(
        id = id,
        fullName = "${user.firstName} ${user.lastName}".trim(),
        email = user.email,
        photoUrl = avatar,
        specialties = specialties.mapNotNull { wrapper ->
            wrapper.specialty?.let { s -> Specialty(id = s.id, name = s.name) }
        },
        plan = DoctorPlan.FREE,
        ratingAverage = ratingAvg,
        ratingsCount = ratingCount,
        consultationFee = consultationPrice,
        homeVisitAvailable = offersHomeVisit,
        virtualVisitAvailable = offersTelemedicine,
        bio = bio,
        isVerified = isActive,
        cmpLicense = licenseNumber,
    )
}

class KtorDoctorSearchDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorSearchDataSource {

    private data class SearchKey(val filters: DoctorFilters, val page: Int)

    private var lastKey: SearchKey? = null
    private var lastResult: PagedDoctors? = null

    override suspend fun searchDoctors(filters: DoctorFilters, page: Int): Result<PagedDoctors> {
        val key = SearchKey(filters, page)
        if (key == lastKey) {
            lastResult?.let { return Result.success(it) }
        }
        return runCatching {
            val response = client.get {
                url("$baseUrl/api/doctors")
                filters.query?.let { parameter("q", it) }
                filters.specialty?.let { parameter("specialtyId", it) }
                filters.minPrice?.let { parameter("priceMin", it) }
                filters.maxPrice?.let { parameter("priceMax", it) }
                filters.minRating?.let { parameter("ratingMin", it) }
                filters.offersTelemedicine?.let { parameter("offersTelemedicine", it) }
                filters.offersHomeVisit?.let { parameter("offersHomeVisit", it) }
                filters.sortBy?.let { parameter("sortBy", it) }
                parameter("page", page)
            }
            val envelope = response.body<PagedApiEnvelope<List<DoctorDto>>>()
            PagedDoctors(
                doctors = envelope.data?.map { it.toDomain() } ?: emptyList(),
                hasMore = envelope.hasMore,
            )
        }.also { result ->
            if (result.isSuccess) {
                lastKey = key
                lastResult = result.getOrThrow()
            }
        }
    }

    override suspend fun getDoctorById(doctorId: String): Result<Doctor> = runCatching {
        client.get {
            url("$baseUrl/api/doctors/$doctorId")
        }.body<ApiEnvelope<DoctorDto>>().data?.toDomain() ?: error("Doctor not found")
    }

    override suspend fun getDoctorReviews(doctorId: String, page: Int): Result<List<Review>> = runCatching {
        client.get {
            url("$baseUrl/api/doctors/$doctorId/reviews")
            parameter("page", page)
        }.body<ApiEnvelope<List<Review>>>().data ?: emptyList()
    }
}
