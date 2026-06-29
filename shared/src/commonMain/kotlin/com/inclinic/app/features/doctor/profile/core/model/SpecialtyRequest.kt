package com.inclinic.app.features.doctor.profile.core.model

/**
 * A doctor's request to be accredited for a new specialty.
 *
 * @param specialtyName  Free-text name when it isn't yet in the catalogue.
 * @param documentUrls   CDN urls of supporting documents (résumé, diploma, etc.).
 * @param comment        Optional note for the admin reviewer.
 */
data class SpecialtyRequest(
    val specialtyName: String,
    val documentUrls: List<String>,
    val comment: String,
)
