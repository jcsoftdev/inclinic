package com.inclinic.app.features.doctor.profile.core.model

import com.inclinic.app.core.model.Specialty

/**
 * Full editable profile for the logged-in doctor (Mi Perfil screen).
 */
data class DoctorProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val photoUrl: String?,
    val bio: String?,
    val specialties: List<Specialty>,
    val consultationFee: Double,
    val supportsPresential: Boolean,
    val supportsVirtual: Boolean,
    val officePhotoUrls: List<String>,
    val cmpLicense: String?,
)
