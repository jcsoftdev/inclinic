package com.inclinic.app.features.patient.presentation.ui

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Relative-time label for past events, Spanish (Peruvian tuteo).
 * e.g. "hace 2 horas", "hace 3 días", "hace un momento".
 */
internal fun relativeTimeLabel(instant: Instant): String {
    val elapsed = Clock.System.now() - instant
    if (elapsed.isNegative()) return "hace un momento"
    val minutes = elapsed.inWholeMinutes
    val hours = elapsed.inWholeHours
    val days = elapsed.inWholeDays
    return when {
        days >= 7 -> "hace ${days / 7} sem"
        days >= 1 -> "hace $days ${if (days == 1L) "día" else "días"}"
        hours >= 1 -> "hace $hours ${if (hours == 1L) "hora" else "horas"}"
        minutes >= 1 -> "hace $minutes min"
        else -> "hace un momento"
    }
}

/**
 * Expiry label relative to now, Spanish (Peruvian tuteo).
 * e.g. "Expira en 6 días", "Expira hoy", "Expirado".
 */
internal fun expiryLabel(expiresAt: Instant): String {
    val left = expiresAt - Clock.System.now()
    if (left.isNegative()) return "Expirado"
    val days = left.inWholeDays
    val hours = left.inWholeHours
    return when {
        days >= 1 -> "Expira en $days ${if (days == 1L) "día" else "días"}"
        hours >= 1 -> "Expira en $hours ${if (hours == 1L) "hora" else "horas"}"
        else -> "Expira hoy"
    }
}
