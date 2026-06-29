package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class TherapyPackage(
    val id: String,
    val offerId: String,
    val doctorId: String,
    val doctorName: String? = null,
    val specialtyName: String? = null,
    val name: String,
    val totalSessions: Int,
    val completedSessions: Int,
    val pricePerSession: Double,
    val totalPrice: Double,
    val discount: Int,
    val status: PackageStatus,
    val paymentDeadline: Instant? = null,
    val createdAt: Instant,
)

@Serializable
enum class PackageStatus { ACTIVE, PENDING_PAYMENT, COMPLETED, CANCELLED, EXPIRED }

@Serializable
data class PackageSession(
    val id: String? = null,
    val sessionNumber: Int,
    val appointmentId: String? = null,
    val scheduledAt: Instant? = null,
    val visitType: VisitType? = null,
    val status: SessionStatus,
)

@Serializable
enum class SessionStatus { SCHEDULED, COMPLETED, CANCELLED, UNSCHEDULED }
