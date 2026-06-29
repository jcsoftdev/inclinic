package com.inclinic.app.features.doctor.no_shows.core.model

/**
 * Doctor-facing domain model for a single no-show appointment.
 *
 * Split source: GET /api/appointments?status=NO_SHOW (doctor JWT auto-scopes doctorId).
 *
 * Tab routing (design):
 *  - Pendientes → [paymentHoldStatus] == [PaymentHoldStatus.HELD]
 *  - Resueltos  → [paymentHoldStatus] == [PaymentHoldStatus.RELEASED] or [PaymentHoldStatus.REFUNDED]
 */
data class NoShowItem(
    val id: String,
    val patientName: String,
    /** ISO-8601 datetime string (e.g. "2026-06-29T10:00:00.000Z"). */
    val startTime: String,
    val price: Double,
    /** Reason the doctor registered when marking no-show; may be null. */
    val reason: String?,
    val specialtyName: String,
    val visitType: String,
    val paymentHoldStatus: PaymentHoldStatus,
) {
    val patientInitials: String
        get() = patientName.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

    val priceLabel: String
        get() {
            val cents = kotlin.math.round(price * 100).toLong()
            val whole = cents / 100
            val frac = kotlin.math.abs(cents % 100)
            return "S/ $whole.${frac.toString().padStart(2, '0')}"
        }
}

enum class PaymentHoldStatus { HELD, RELEASED, REFUNDED, UNKNOWN }
