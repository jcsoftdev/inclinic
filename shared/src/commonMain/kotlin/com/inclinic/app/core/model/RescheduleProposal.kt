package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RescheduleProposal(
    val id: String,
    val appointmentId: String,
    val requestedBy: String,
    val proposedStart: Instant,
    val proposedEnd: Instant,
    val reason: String?,
    val status: RescheduleStatus,
    val expiresAt: Instant,
    val createdAt: Instant,
    val doctorName: String? = null,
    val specialtyName: String? = null,
    val originalStart: Instant? = null,
    val visitType: VisitType = VisitType.CLINIC,
)

@Serializable
enum class RescheduleStatus {
    PENDING, APPROVED, REJECTED, CANCELLED, EXPIRED
}
