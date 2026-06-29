package com.inclinic.app.features.doctor.modality.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ModalityChangeRequestDto(
    val id: String,
    val patientName: String,
    val patientSubtitle: String? = null,
    val appointmentSlot: String,
    val currentModality: String,
    val requestedModality: String,
    val reason: String? = null,
    val address: String? = null,
    val suggestedPrice: Int? = null,
    val status: String,
)

@Serializable
data class RespondModalityChangeDto(
    val action: String,
    val adjustedPrice: Int? = null,
)
