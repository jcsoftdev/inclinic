package com.inclinic.app.features.doctor.reschedule.core.model

enum class RescheduleRequestStatus { PENDING, APPROVED, REJECTED, EXPIRED, UNKNOWN }

data class RescheduleRequest(
    val id: String,
    val patientName: String,
    val currentSlot: String,
    val requestedSlot: String,
    val reason: String?,
    val status: RescheduleRequestStatus,
    /** Short human date label shown on the queue card, e.g. "Mar 22 mayo". Falls back to [currentSlot] when absent. */
    val dateLabel: String? = null,
)
