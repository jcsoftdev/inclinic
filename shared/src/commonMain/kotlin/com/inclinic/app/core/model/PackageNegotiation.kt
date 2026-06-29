package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PackageNegotiation(
    val id: String,
    val offerId: String,
    val offerName: String? = null,
    val doctorName: String? = null,
    val status: NegotiationStatus,
    val proposals: List<NegotiationProposal> = emptyList(),
    val finalPricePerSession: Double? = null,
    val finalSessions: Int? = null,
    val acceptedTherapyPackageId: String? = null,
)

@Serializable
data class NegotiationProposal(
    val id: String,
    val proposedBy: String,           // "DOCTOR" | "PATIENT"
    val pricePerSession: Double,
    val sessions: Int,
    val message: String? = null,
    val createdAt: Instant,
)

@Serializable
enum class NegotiationStatus { PENDING_DOCTOR, PENDING_PATIENT, ACCEPTED, REJECTED, EXPIRED, PAID }
