package com.inclinic.app.features.doctor.packages.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Mirrors the backend `TherapyPackage` payload from `/api/therapy-packages`.
 * The route includes nested `patient`, `specialty` and (optionally) `sessions`.
 */
@Serializable
data class TherapyPackageDto(
    val id: String,
    val patientId: String,
    val specialtyId: String,
    val packageName: String,
    val totalSessions: Int,
    val regularPricePerSession: Double,
    val packagePricePerSession: Double,
    val isPrepaid: Boolean = false,
    val prepaidDiscount: Double? = null,
    val totalPrepaidAmount: Double? = null,
    val sessionsCompleted: Int = 0,
    val sessionsScheduled: Int = 0,
    val sessionsUsed: Int = 0,
    val status: String = "PENDING_PAYMENT",
    val patient: PackagePartyDto? = null,
    val specialty: PackageSpecialtyDto? = null,
    val sessions: List<PackageSessionDto> = emptyList(),
)

@Serializable
data class PackagePartyDto(
    val id: String? = null,
    val user: PackageUserDto? = null,
)

@Serializable
data class PackageUserDto(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
)

@Serializable
data class PackageSpecialtyDto(
    val id: String? = null,
    val name: String? = null,
)

@Serializable
data class PackageSessionDto(
    val id: String,
    val status: String? = null,
    val startTime: String? = null,
    val sessionNumber: Int? = null,
)

/**
 * Create body for `POST /api/therapy-packages` (`createTherapyPackageSchema`):
 * a doctor proposes a package to a specific patient.
 */
@Serializable
data class CreatePackageRequestDto(
    val patientId: String,
    val specialtyId: String,
    val packageName: String,
    val totalSessions: Int,
    val regularPricePerSession: Double,
    val packagePricePerSession: Double,
    val isPrepaid: Boolean = false,
    val prepaidDiscount: Double? = null,
    val isHomeVisit: Boolean = false,
)
