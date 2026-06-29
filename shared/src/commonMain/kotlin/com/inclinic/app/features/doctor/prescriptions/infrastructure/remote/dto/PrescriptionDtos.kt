package com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Mirrors the backend Prescription entity.
 * GET /api/prescriptions/{id}  ->  {success, data: PrescriptionDto}
 * PUT /api/prescriptions/{id}  ->  {success, data: PrescriptionDto}
 */
@Serializable
data class PrescriptionDto(
    val id: String,
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val diagnosis: String? = null,
    val instructions: String? = null,
    val notes: String? = null,
    val validUntil: String? = null,
    val doctorFullName: String = "",
    val doctorSignature: String = "",
    val createdAt: String = "",
    val items: List<PrescriptionItemDto> = emptyList(),
)

@Serializable
data class PrescriptionItemDto(
    val id: String = "",
    val medicationName: String,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val notes: String? = null,
    val order: Int = 0,
)

/**
 * PUT /api/prescriptions/{id} body.
 * Matches Partial<CreatePrescriptionInput> in prescription.service.ts.
 */
@Serializable
data class UpdatePrescriptionRequestDto(
    val diagnosis: String? = null,
    val instructions: String? = null,
    val notes: String? = null,
    val validUntil: String? = null,
    val items: List<UpdatePrescriptionItemDto> = emptyList(),
)

@Serializable
data class UpdatePrescriptionItemDto(
    val medicationName: String,
    val dosage: String? = null,
    val frequency: String? = null,
    val duration: String? = null,
    val notes: String? = null,
    val order: Int? = null,
)
