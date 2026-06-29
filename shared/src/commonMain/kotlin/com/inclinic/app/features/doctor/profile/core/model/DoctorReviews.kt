package com.inclinic.app.features.doctor.profile.core.model

/**
 * A single patient review for the doctor.
 *
 * Sourced from GET /api/doctors/{id}/reviews.
 */
data class DoctorReview(
    val id: String,
    val rating: Int,
    val comment: String?,
    val date: String?,
    val specialty: String?,
    val patientInitials: String,
)

/**
 * Aggregated review summary + list of individual reviews.
 *
 * Sourced from GET /api/doctors/{id}/reviews.
 */
data class DoctorReviewsPage(
    val averageRating: Double,
    val totalRatings: Int,
    val reviews: List<DoctorReview>,
)
