package com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ShareRequestDto(
    val id: String,
    val patientId: String,
    val requesterDoctorId: String,
    val reason: String,
    val scope: String = "FULL_HISTORY",
    val status: String,
    val createdAt: String,
    val expiresAt: String? = null,
    val accessExpiresAt: String? = null,
    // Nested patient info from Prisma include
    val patient: PatientUserDto? = null,
)

@Serializable
data class PatientUserDto(
    val user: PatientUserInfoDto? = null,
)

@Serializable
data class PatientUserInfoDto(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
)

@Serializable
data class CreateShareRequestDto(
    val patientId: String,
    val reason: String,
    val scope: String = "FULL_HISTORY",
)

/** Used by doctor to cancel a pending request (DELETE /api/medical-history-share/{id}) — no body needed */
// No DTO needed for DELETE
