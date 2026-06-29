package com.inclinic.app.features.auth.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request body for patient self-registration.
 * Maps to the backend's [patientRegisterSchema]:
 *   firstName (min 2), lastName (min 2), email, password (min 6), phone (optional).
 */
@Serializable
data class PatientRegisterRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String? = null,
)
