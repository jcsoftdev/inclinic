package com.inclinic.app.features.doctor.pending_closure.core.model

/**
 * Doctor-facing domain model for a single appointment pending manual closure.
 *
 * Source: GET /api/appointments?needsClosure=true (doctor JWT auto-scopes doctorId).
 * "Needs closure" = CONFIRMED or IN_PROGRESS whose endsAt passed more than 2h ago
 * without the doctor marking it COMPLETED or NO_SHOW — see appointment.service.ts
 * computeNeedsClosure().
 */
data class PendingClosureItem(
    val id: String,
    val patientName: String,
    /** ISO-8601 datetime string (e.g. "2026-06-29T10:00:00.000Z"). */
    val startTime: String,
    val price: Double,
    val specialtyName: String,
    val visitType: String,
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
