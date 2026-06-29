package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.Review

data class PagedDoctors(val doctors: List<Doctor>, val hasMore: Boolean)

/**
 * All filterable criteria for a doctor search, collapsed into a single value object.
 *
 * Grouping these (instead of a long positional parameter list) prevents the
 * adjacent-nullable bug class — `minPrice`, `maxPrice` and `minRating` are all
 * `Double?` and would be trivial to transpose as positional args — and lets new
 * filters be added without churning every call site / fake.
 */
data class DoctorFilters(
    val query: String? = null,
    val specialty: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minRating: Double? = null,
    val offersTelemedicine: Boolean? = null,
    val offersHomeVisit: Boolean? = null,
    val sortBy: String? = null,
)

interface DoctorSearchDataSource {
    suspend fun searchDoctors(filters: DoctorFilters, page: Int): Result<PagedDoctors>

    suspend fun getDoctorById(doctorId: String): Result<Doctor>

    suspend fun getDoctorReviews(doctorId: String, page: Int): Result<List<Review>>
}
