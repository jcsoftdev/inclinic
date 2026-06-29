package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TherapyOffer(
    val id: String,
    val doctorId: String,
    val doctorName: String? = null,
    val specialtyName: String? = null,
    val name: String,
    val sessions: Int,
    val visitTypes: List<VisitType> = emptyList(),
    val description: String? = null,
    val pricePerSession: Double,
    val originalPrice: Double? = null,
    val isNegotiable: Boolean = false,
)
