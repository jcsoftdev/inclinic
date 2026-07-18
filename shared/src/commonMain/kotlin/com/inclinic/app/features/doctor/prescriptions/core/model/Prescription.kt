package com.inclinic.app.features.doctor.prescriptions.core.model

data class PrescriptionItem(
    val id: String,
    val medicationName: String,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val notes: String?,
    val order: Int,
)

data class Prescription(
    val id: String,
    val appointmentId: String,
    val doctorId: String,
    val patientId: String,
    val diagnosis: String?,
    val instructions: String?,
    val notes: String?,
    val validUntil: String?,
    val doctorFullName: String,
    val doctorSignature: String,
    val items: List<PrescriptionItem>,
    val createdAt: String,
)

/** Payload for PUT /api/prescriptions/{id}. */
data class UpdatePrescriptionDraft(
    val diagnosis: String?,
    val instructions: String?,
    val notes: String?,
    val validUntil: String?,
    /** When non-empty, replaces all existing items. */
    val items: List<PrescriptionItemDraft> = emptyList(),
)

data class PrescriptionItemDraft(
    val medicationName: String,
    val dosage: String?,
    val frequency: String?,
    val duration: String?,
    val notes: String?,
    val order: Int? = null,
)

/** Payload for POST /api/prescriptions. */
data class CreatePrescriptionDraft(
    val appointmentId: String,
    val diagnosis: String?,
    val instructions: String?,
    val notes: String?,
    val validUntil: String?,
    val items: List<PrescriptionItemDraft>,
)
