package com.inclinic.app.features.auth.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Request DTO for POST /api/doctors/freelance (public endpoint).
 *
 * Field names match the backend [createFreelanceDoctorSchema] exactly.
 * No password field — doctor activates the account separately via email link.
 */
@Serializable
data class DoctorFreelanceRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val licenseNumber: String? = null,
    val bio: String? = null,
    val avatar: String? = null,
    val photos: List<String>? = null,
    /** At least one document URL required by backend. */
    val documents: List<String>,
    /** "BY_SCHEDULE" | "BY_TURN" */
    val appointmentMode: String = "BY_SCHEDULE",
    /** Minutes; min 10, default 30. */
    val appointmentDuration: Int = 30,
    val maxPatientsPerDay: Int? = null,
    /** At least one specialtyId required. */
    val specialtyIds: List<String>,
    val primarySpecialtyId: String,
    /** Minimum 50 (backend rule). */
    val consultationPrice: Double,
    val offersHomeVisit: Boolean = false,
    val serviceArea: String? = null,
    /** At least one schedule required. */
    val schedules: List<FreelanceScheduleDto>,
)

/**
 * One day-of-week schedule block inside [DoctorFreelanceRequestDto].
 *
 * [dayOfWeek] must be one of MONDAY..SUNDAY (uppercase, matches backend enum).
 * [startTime] and [endTime] are "HH:MM" strings.
 */
@Serializable
data class FreelanceScheduleDto(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val breakStart: String? = null,
    val breakEnd: String? = null,
    val maxPatients: Int = 8,
    val slotDuration: Int? = null,
    val price: Double? = null,
    val specialtyId: String? = null,
    val isHomeVisit: Boolean? = null,
)
