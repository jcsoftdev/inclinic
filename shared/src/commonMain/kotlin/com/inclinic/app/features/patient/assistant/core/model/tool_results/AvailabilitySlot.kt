package com.inclinic.app.features.patient.assistant.core.model.tool_results

/**
 * A single availability slot from the `getDoctorAvailability` tool.
 *
 * [time] is HH:MM (e.g. "10:00") — the doctor's local slot time.
 * [available] — false means the slot is already booked.
 */
data class AvailabilitySlot(
    val time: String,
    val available: Boolean,
)
