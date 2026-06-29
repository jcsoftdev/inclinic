package com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RescheduleRequestDto(
    val id: String,
    val patientName: String,
    val currentSlot: String,
    val requestedSlot: String,
    val reason: String? = null,
    val status: String,
    val dateLabel: String? = null,
)

@Serializable
data class RespondRescheduleRequestDto(
    val action: String,
)
