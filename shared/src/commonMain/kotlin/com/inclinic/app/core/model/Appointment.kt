package com.inclinic.app.core.model

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Appointment(
    val id: String,
    val doctorId: String,
    val patientId: String,
    val specialtyId: String,
    val visitType: VisitType,
    val status: AppointmentStatus,
    val consultationFee: Double,
    val commissionAmount: Double,
    val startsAt: Instant,
    val endsAt: Instant,
    val rescheduleCount: Int,
    val paymentDeadline: Instant?,
    val notes: String?,
    val createdAt: Instant,
    val doctorName: String? = null,
    val specialtyName: String? = null,
    val isPackageSession: Boolean = false,
    val hasPendingReschedule: Boolean = false,
    val needsClosure: Boolean = false,
)

@Serializable
enum class VisitType { VIRTUAL, HOME, CLINIC }

@Serializable
enum class AppointmentStatus {
    PENDING_PAYMENT,
    SCHEDULED,
    CONFIRMED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED_BY_PATIENT,
    CANCELLED_BY_DOCTOR,
    NO_SHOW,
    DISPUTED,
    REFUNDED,
}
