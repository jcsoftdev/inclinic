package com.inclinic.app.features.doctor.profile.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Specialty
import com.inclinic.app.features.doctor.profile.core.model.DoctorProfile
import com.inclinic.app.features.doctor.profile.core.model.DoctorReview
import com.inclinic.app.features.doctor.profile.core.model.DoctorReviewsPage
import com.inclinic.app.features.doctor.profile.core.model.IncomeBreakdown
import com.inclinic.app.features.doctor.profile.core.model.IncomeSummary
import com.inclinic.app.features.doctor.profile.core.model.MySpecialtyRequest
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequest
import com.inclinic.app.features.doctor.profile.core.port.DoctorProfileRepository
import com.inclinic.app.features.doctor.profile.infrastructure.remote.DoctorProfileDto
import com.inclinic.app.features.doctor.profile.infrastructure.remote.DoctorProfileExtendedDataSource
import com.inclinic.app.features.doctor.profile.infrastructure.remote.ReviewItemDto
import com.inclinic.app.features.doctor.profile.infrastructure.remote.UpdateProfileRequestDto
import kotlinx.coroutines.withContext

class DefaultDoctorProfileRepository(
    private val remote: DoctorProfileExtendedDataSource,
    private val dispatchers: AppDispatchers,
    private val doctorId: String,
) : DoctorProfileRepository {

    override suspend fun getProfile(): Result<DoctorProfile> =
        withContext(dispatchers.io) {
            remote.getProfile(doctorId).map { it.toDomain() }
        }

    override suspend fun updateProfile(profile: DoctorProfile): Result<DoctorProfile> =
        withContext(dispatchers.io) {
            remote.updateProfile(
                doctorId,
                UpdateProfileRequestDto(
                    bio = profile.bio,
                    consultationPrice = profile.consultationFee,
                    offersHomeVisit = profile.supportsPresential,
                    offersTelemedicine = profile.supportsVirtual,
                    licenseNumber = profile.cmpLicense,
                )
            ).map { it.toDomain() }
        }

    override suspend fun editSpecialties(specialtyIds: List<String>): Result<Unit> =
        withContext(dispatchers.io) { remote.editSpecialties(doctorId, specialtyIds) }

    override suspend fun requestSpecialty(request: SpecialtyRequest): Result<Unit> {
        // No backend endpoint exists yet for specialty accreditation requests.
        return Result.failure(NotImplementedError("Endpoint de solicitud de especialidad no disponible aun"))
    }

    override suspend fun getMySpecialtyRequests(): Result<List<MySpecialtyRequest>> {
        // No backend endpoint exists yet for listing specialty requests.
        return Result.success(emptyList())
    }

    override suspend fun getIncome(): Result<IncomeSummary> =
        withContext(dispatchers.io) {
            remote.getMetrics().map { metrics ->
                val mr = metrics.monthRevenue
                IncomeSummary(
                    totalCents = ((mr?.amount ?: 0.0) * 100).toLong(),
                    commissionCents = ((mr?.commission ?: 0.0) * 100).toLong(),
                    netCents = ((mr?.net ?: 0.0) * 100).toLong(),
                    sessions = mr?.sessions ?: 0,
                    growthPct = mr?.growthPct,
                    availableCents = 0L,
                    bars = emptyList(),
                    breakdown = mr?.breakdown?.let { bd ->
                        IncomeBreakdown(
                            retainedCents = (bd.retained * 100).toLong(),
                            releasedCents = (bd.released * 100).toLong(),
                            refundedCents = (bd.refunded * 100).toLong(),
                        )
                    },
                )
            }
        }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> =
        withContext(dispatchers.io) {
            remote.changePassword(currentPassword, newPassword)
        }

    override suspend fun getReviews(limit: Int): Result<DoctorReviewsPage> =
        withContext(dispatchers.io) {
            remote.getReviews(doctorId, limit).map { dto ->
                DoctorReviewsPage(
                    averageRating = dto.averageRating,
                    totalRatings = dto.totalRatings,
                    reviews = dto.reviews.map { it.toDomain() },
                )
            }
        }

    private fun DoctorProfileDto.toDomain(): DoctorProfile {
        val fullName = "${user?.firstName.orEmpty()} ${user?.lastName.orEmpty()}".trim()
        return DoctorProfile(
            id = id,
            fullName = fullName.ifBlank { id },
            email = user?.email.orEmpty(),
            photoUrl = avatar,
            bio = bio,
            specialties = specialties.mapNotNull { ds ->
                ds.specialty?.let { sp -> Specialty(id = sp.id, name = sp.name, slug = sp.slug) }
            },
            consultationFee = consultationPrice,
            supportsPresential = offersHomeVisit,
            supportsVirtual = offersTelemedicine,
            officePhotoUrls = photos,
            cmpLicense = licenseNumber,
        )
    }

    private fun ReviewItemDto.toDomain() = DoctorReview(
        id = id,
        rating = rating ?: 0,
        comment = comment,
        date = date,
        specialty = specialty,
        patientInitials = patientInitials,
    )
}
