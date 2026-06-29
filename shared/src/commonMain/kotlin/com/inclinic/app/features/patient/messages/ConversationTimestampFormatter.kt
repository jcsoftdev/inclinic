package com.inclinic.app.features.patient.messages

import kotlin.time.Instant
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

/**
 * Formats a conversation's [lastMessageAt] Instant into a human-readable string:
 *
 * - Same day as [now]  → "HH:mm"  (e.g. "10:30")
 * - Yesterday relative to [now] → "Ayer"
 * - Older              → "DD/MM"  (e.g. "15/03")
 *
 * The [timeZone] parameter defaults to [TimeZone.currentSystemDefault] but is
 * injectable so the function can be tested deterministically.
 */
fun formatConversationTimestamp(
    instant: Instant,
    now: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val localInstant = instant.toLocalDateTime(timeZone)
    val localNow = now.toLocalDateTime(timeZone)

    val instantDate = localInstant.date
    val nowDate = localNow.date
    val yesterday = nowDate.minus(DatePeriod(days = 1))

    return when {
        instantDate == nowDate -> {
            // Same calendar day → "HH:mm"
            val h = localInstant.hour.toString().padStart(2, '0')
            val m = localInstant.minute.toString().padStart(2, '0')
            "$h:$m"
        }
        instantDate == yesterday -> "Ayer"
        else -> {
            // Older → "DD/MM"
            val d = localInstant.dayOfMonth.toString().padStart(2, '0')
            val mo = localInstant.monthNumber.toString().padStart(2, '0')
            "$d/$mo"
        }
    }
}
