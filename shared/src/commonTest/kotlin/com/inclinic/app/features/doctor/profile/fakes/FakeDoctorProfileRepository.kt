package com.inclinic.app.features.doctor.profile.fakes

import com.inclinic.app.core.model.Specialty
import com.inclinic.app.features.doctor.profile.core.model.DoctorProfile
import com.inclinic.app.features.doctor.profile.core.model.DoctorReview
import com.inclinic.app.features.doctor.profile.core.model.DoctorReviewsPage
import com.inclinic.app.features.doctor.profile.core.model.IncomeBar
import com.inclinic.app.features.doctor.profile.core.model.IncomeBreakdown
import com.inclinic.app.features.doctor.profile.core.model.IncomeSummary
import com.inclinic.app.features.doctor.profile.core.model.MySpecialtyRequest
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequest
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequestStatus
import com.inclinic.app.features.doctor.profile.core.port.DoctorProfileRepository

/**
 * In-memory fake for [DoctorProfileRepository].
 * Tests configure results before calling; call counts verify no-network guarantees.
 */
class FakeDoctorProfileRepository : DoctorProfileRepository {

    companion object {
        val defaultProfile = DoctorProfile(
            id = "doc-1",
            fullName = "Dr. Carlos Ramos",
            email = "carlos@inclinic.com",
            photoUrl = null,
            bio = "Cardiólogo con 15 años de experiencia.",
            specialties = listOf(Specialty(id = "sp-1", name = "Cardiología", slug = "cardiology")),
            consultationFee = 120.0,
            supportsPresential = true,
            supportsVirtual = false,
            officePhotoUrls = emptyList(),
            cmpLicense = "CMP-12345",
        )

        val defaultIncome = IncomeSummary(
            totalCents = 18000L,
            commissionCents = 2700L,
            netCents = 15300L,
            sessions = 6,
            growthPct = 12.0,
            availableCents = 0L,
            bars = emptyList(),
            breakdown = IncomeBreakdown(
                retainedCents = 50000L,
                releasedCents = 100000L,
                refundedCents = 30000L,
            ),
        )

        val defaultRequests = listOf(
            MySpecialtyRequest(
                id = "req-1",
                specialtyName = "Cardiología pediátrica",
                status = SpecialtyRequestStatus.Pending,
                createdAt = "2026-03-12",
                documentCount = 3,
            ),
            MySpecialtyRequest(
                id = "req-2",
                specialtyName = "Geriatría",
                status = SpecialtyRequestStatus.Approved,
                createdAt = "2026-03-02",
            ),
            MySpecialtyRequest(
                id = "req-3",
                specialtyName = "Cirugía plástica",
                status = SpecialtyRequestStatus.Rejected,
                createdAt = "2026-02-18",
                rejectionReason = "Diploma no acreditado por CMP.",
            ),
        )

        val defaultReviews = DoctorReviewsPage(
            averageRating = 4.8,
            totalRatings = 2,
            reviews = listOf(
                DoctorReview(
                    id = "rev-1",
                    rating = 5,
                    comment = "Excelente trato, muy claro explicando el diagnóstico.",
                    date = "2026-04-01",
                    specialty = "Cardiología",
                    patientInitials = "A. R.",
                ),
                DoctorReview(
                    id = "rev-2",
                    rating = 4,
                    comment = "Puntual y profesional.",
                    date = "2026-03-15",
                    specialty = null,
                    patientInitials = "M. V.",
                ),
            ),
        )
    }

    var getProfileResult: Result<DoctorProfile> = Result.success(defaultProfile)
    var updateProfileResult: Result<DoctorProfile> = Result.success(defaultProfile)
    var editSpecialtiesResult: Result<Unit> = Result.success(Unit)
    var requestSpecialtyResult: Result<Unit> = Result.success(Unit)
    var getMySpecialtyRequestsResult: Result<List<MySpecialtyRequest>> = Result.success(defaultRequests)
    var getIncomeResult: Result<IncomeSummary> = Result.success(defaultIncome)
    var getReviewsResult: Result<DoctorReviewsPage> = Result.success(defaultReviews)
    var changePasswordResult: Result<Unit> = Result.success(Unit)

    var getProfileCallCount = 0
    var updateProfileCallCount = 0
    var editSpecialtiesCallCount = 0
    var requestSpecialtyCallCount = 0
    var getMySpecialtyRequestsCallCount = 0
    var getIncomeCallCount = 0
    var getReviewsCallCount = 0
    var changePasswordCallCount = 0

    var lastUpdatedProfile: DoctorProfile? = null
    var lastEditedSpecialtyIds: List<String>? = null
    var lastSpecialtyRequest: SpecialtyRequest? = null
    var lastReviewsLimit: Int? = null
    var lastChangePasswordCurrent: String? = null
    var lastChangePasswordNew: String? = null

    override suspend fun getProfile(): Result<DoctorProfile> {
        getProfileCallCount++
        return getProfileResult
    }

    override suspend fun updateProfile(profile: DoctorProfile): Result<DoctorProfile> {
        updateProfileCallCount++
        lastUpdatedProfile = profile
        return updateProfileResult
    }

    override suspend fun editSpecialties(specialtyIds: List<String>): Result<Unit> {
        editSpecialtiesCallCount++
        lastEditedSpecialtyIds = specialtyIds
        return editSpecialtiesResult
    }

    override suspend fun requestSpecialty(request: SpecialtyRequest): Result<Unit> {
        requestSpecialtyCallCount++
        lastSpecialtyRequest = request
        return requestSpecialtyResult
    }

    override suspend fun getMySpecialtyRequests(): Result<List<MySpecialtyRequest>> {
        getMySpecialtyRequestsCallCount++
        return getMySpecialtyRequestsResult
    }

    override suspend fun getIncome(): Result<IncomeSummary> {
        getIncomeCallCount++
        return getIncomeResult
    }

    override suspend fun getReviews(limit: Int): Result<DoctorReviewsPage> {
        getReviewsCallCount++
        lastReviewsLimit = limit
        return getReviewsResult
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        changePasswordCallCount++
        lastChangePasswordCurrent = currentPassword
        lastChangePasswordNew = newPassword
        return changePasswordResult
    }
}
