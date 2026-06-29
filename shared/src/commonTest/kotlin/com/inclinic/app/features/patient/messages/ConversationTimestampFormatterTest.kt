package com.inclinic.app.features.patient.messages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/**
 * Tests for [formatConversationTimestamp].
 *
 * All assertions use UTC to stay deterministic across devices.
 */
class ConversationTimestampFormatterTest {

    private val tz = TimeZone.UTC

    // Same day → "HH:mm"
    @Test
    fun same_day_message_formats_as_time() {
        val now = Instant.parse("2026-06-15T14:30:00Z")
        val lastMessage = Instant.parse("2026-06-15T10:30:00Z")

        val result = formatConversationTimestamp(lastMessage, now, tz)

        assertEquals("10:30", result)
    }

    @Test
    fun same_day_midnight_formats_as_time() {
        val now = Instant.parse("2026-06-15T23:59:00Z")
        val lastMessage = Instant.parse("2026-06-15T00:05:00Z")

        val result = formatConversationTimestamp(lastMessage, now, tz)

        assertEquals("00:05", result)
    }

    // Yesterday → "Ayer"
    @Test
    fun yesterday_message_formats_as_Ayer() {
        val now = Instant.parse("2026-06-15T09:00:00Z")
        val lastMessage = Instant.parse("2026-06-14T22:00:00Z")

        val result = formatConversationTimestamp(lastMessage, now, tz)

        assertEquals("Ayer", result)
    }

    // Older → "DD/MM"
    @Test
    fun older_message_formats_as_day_slash_month() {
        val now = Instant.parse("2026-06-15T14:30:00Z")
        val lastMessage = Instant.parse("2026-03-07T10:00:00Z")

        val result = formatConversationTimestamp(lastMessage, now, tz)

        assertEquals("07/03", result)
    }

    @Test
    fun older_message_zero_pads_single_digit_day_and_month() {
        val now = Instant.parse("2026-06-15T14:30:00Z")
        val lastMessage = Instant.parse("2026-01-05T08:00:00Z")

        val result = formatConversationTimestamp(lastMessage, now, tz)

        assertEquals("05/01", result)
    }
}
