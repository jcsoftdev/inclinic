package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AvailabilitySlot(
    val id: String,
    val startTime: String,
    val endTime: String,
    val isAvailable: Boolean,
)
