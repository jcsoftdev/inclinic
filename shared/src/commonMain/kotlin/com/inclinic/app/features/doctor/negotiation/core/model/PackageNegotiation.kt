package com.inclinic.app.features.doctor.negotiation.core.model

enum class PackageNegotiationStatus { PENDING, ACCEPTED, REJECTED, COUNTERED, EXPIRED, UNKNOWN }

enum class NegotiationAction { ACCEPT, REJECT, COUNTER }

data class PackageNegotiation(
    val id: String,
    val patientName: String,
    val packageName: String,
    val originalPriceCents: Int,
    val proposedPriceCents: Int,
    val message: String?,
    val status: PackageNegotiationStatus,
)
