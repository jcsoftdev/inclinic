package com.inclinic.app.features.patient.assistant.infrastructure.dto

import kotlinx.serialization.Serializable

/**
 * Wire DTOs for tool results returned by the ClinicAI assistant backend.
 * Each DTO mirrors the Zod output schema of the corresponding tool.
 * `toDomain()` mappers live in [DtoMappers].
 */

@Serializable
data class DoctorResultDto(
    val id: String,
    val name: String,
    val bio: String,
    val consultationPrice: Double,
    val ratingAvg: Double? = null,
    val ratingCount: Int,
)

@Serializable
data class SpecialtyResultDto(
    val id: String,
    val name: String,
    val description: String,
    val icon: String? = null,
)

@Serializable
data class AvailabilitySlotDto(
    val time: String,
    val available: Boolean,
)

/**
 * Booking result DTO.
 *
 * On success: `ok = true`, [appointmentId] and [paymentRedirectPath] are present.
 * On failure: `ok = false`, [error] and [message] are present.
 *
 * Branching to the sealed [com.inclinic.app.features.patient.assistant.core.model.tool_results.BookingResult]
 * is done in [DtoMappers.bookingResultDtoToDomain].
 */
@Serializable
data class BookingResultDto(
    val ok: Boolean,
    val appointmentId: String? = null,
    val paymentRedirectPath: String? = null,
    val error: String? = null,
    val message: String? = null,
)
