package com.inclinic.app.features.doctor.profile.core.model

/**
 * A doctor's request to be accredited for a new specialty.
 *
 * @param specialtyId   Id of the specialty from the catalogue (GET /api/specialties).
 * @param documentUrls  CDN urls of supporting documents (certification, diploma, …). At least one.
 * @param comment       Optional note for the admin reviewer.
 */
data class SpecialtyRequest(
    val specialtyId: String,
    val documentUrls: List<String>,
    val comment: String,
)
