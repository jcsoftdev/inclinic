package com.inclinic.app.features.doctor.profile.core.port

import com.inclinic.app.features.doctor.profile.core.model.DoctorProfile
import com.inclinic.app.features.doctor.profile.core.model.DoctorReviewsPage
import com.inclinic.app.features.doctor.profile.core.model.IncomeSummary
import com.inclinic.app.features.doctor.profile.core.model.MySpecialtyRequest
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequest

interface DoctorProfileRepository {
    /** Fetch the full editable profile of the currently-authenticated doctor. */
    suspend fun getProfile(): Result<DoctorProfile>

    /** Persist changes to bio, fee, modalities, etc. */
    suspend fun updateProfile(profile: DoctorProfile): Result<DoctorProfile>

    /**
     * Replace the doctor's active specialty list.
     * @param specialtyIds Ordered list of specialty ids to keep.
     */
    suspend fun editSpecialties(specialtyIds: List<String>): Result<Unit>

    /**
     * Submit a request to be accredited for a new specialty.
     *
     * NOTE: No dedicated backend endpoint exists yet for this feature.
     * Returns failure until backend adds POST /api/doctors/{id}/specialty-requests.
     */
    suspend fun requestSpecialty(request: SpecialtyRequest): Result<Unit>

    /**
     * List the authenticated doctor's own specialty-accreditation requests.
     *
     * NOTE: No dedicated backend endpoint exists yet. Returns empty list.
     */
    suspend fun getMySpecialtyRequests(): Result<List<MySpecialtyRequest>>

    /**
     * Return aggregated income data from GET /api/doctors/me/metrics.
     * The [IncomeSummary.bars] will always be empty (no time-series on backend).
     */
    suspend fun getIncome(): Result<IncomeSummary>

    /**
     * Return the doctor's public reviews from GET /api/doctors/{id}/reviews.
     * @param limit Max number of reviews to return (default 20).
     */
    suspend fun getReviews(limit: Int = 20): Result<DoctorReviewsPage>

    /**
     * Change the authenticated user's password via PATCH /api/users/me/password.
     * Returns failure with message "INVALID_CREDENTIALS" when current password is wrong.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
}
