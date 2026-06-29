package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ChangeVisitTypeRequest(
    val appointmentId: String,
    val newVisitType: String,
    val reason: String? = null,
    val address: String? = null,
)
