package com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRescheduleRequestDto(
    val proposedSlot: String,
    val message: String? = null,
)
