package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val patientName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: Instant,
)
