package com.inclinic.app.features.doctor.modality.core.model

enum class ModalityRequestStatus { PENDING, APPROVED, REJECTED, EXPIRED, UNKNOWN }

enum class ModalityResponseAction { APPROVE, REJECT }

data class ModalityChangeRequest(
    val id: String,
    val patientName: String,
    val patientSubtitle: String? = null,
    val appointmentSlot: String,
    val currentModality: String,
    val requestedModality: String,
    val reason: String?,
    val address: String? = null,
    val suggestedPrice: Int? = null,
    val status: ModalityRequestStatus,
)
