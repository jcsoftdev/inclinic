package com.inclinic.app.features.doctor.negotiation.infrastructure.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PackageNegotiationDto(
    val id: String,
    val patientName: String,
    val packageName: String,
    val originalPriceCents: Int,
    val proposedPriceCents: Int,
    val message: String? = null,
    val status: String,
)

@Serializable
data class RespondNegotiationDto(
    val action: String,
    val counterPriceCents: Int? = null,
)
