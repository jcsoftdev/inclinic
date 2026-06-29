package com.inclinic.app.features.patient.assistant.infrastructure.dto

import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import com.inclinic.app.features.patient.assistant.core.model.ToolName
import com.inclinic.app.features.patient.assistant.core.model.tool_results.AvailabilitySlot
import com.inclinic.app.features.patient.assistant.core.model.tool_results.BookingResult
import com.inclinic.app.features.patient.assistant.core.model.tool_results.DoctorResult
import com.inclinic.app.features.patient.assistant.core.model.tool_results.SpecialtyResult

/**
 * Maps [UIMessageChunkDto] to a domain [AssistantStreamEvent], or returns null when the
 * chunk is an envelope-only type that should be silently skipped.
 *
 * Envelope chunks that return null:
 *   start, start-step, finish-step, text-start, text-end, abort,
 *   tool-input-start, tool-input-delta, tool-output-error
 *
 * Also returns null when a required field is missing (caller skips via `?.let { emit(it) }`).
 */
fun UIMessageChunkDto.toStreamEvent(): AssistantStreamEvent? = when (type) {

    "text-delta" -> delta?.let { AssistantStreamEvent.TextDelta(it) }

    "tool-input-available" -> {
        val id = toolCallId ?: return null
        val name = toolName?.let { ToolName.fromBackend(it) } ?: return null
        val args = input ?: return null
        AssistantStreamEvent.ToolCallStarted(
            toolCallId = id,
            toolName = name,
            args = args,
        )
    }

    "tool-output-available" -> {
        val id = toolCallId ?: return null
        val rawOutput = output ?: return null

        // toolName is NOT in tool-output-available chunks in Vercel AI SDK v6.
        // We emit ToolResult with a sentinel UNKNOWN toolName that the component
        // resolves by matching toolCallId against the activeToolCall it tracked.
        // Phase 5 data source tests verify this behaviour.
        AssistantStreamEvent.ToolResult(
            toolCallId = id,
            toolName = ToolName.fromBackend(toolName ?: "") ?: ToolName.SEARCH_DOCTORS,
            result = rawOutput,
        )
    }

    "finish" -> AssistantStreamEvent.Finish

    "error" -> AssistantStreamEvent.Error(
        code = "STREAM_ERROR",
        retryAfterSeconds = null,
    )

    // Envelope-only chunk types — skip silently
    "start",
    "start-step",
    "finish-step",
    "text-start",
    "text-end",
    "abort",
    "tool-input-start",
    "tool-input-delta",
    "tool-output-error" -> null

    // Unknown future chunk types — skip silently
    else -> null
}

// ── Tool-result DTO → domain ─────────────────────────────────────────────────

fun DoctorResultDto.toDomain() = DoctorResult(
    id = id,
    name = name,
    bio = bio,
    consultationPrice = consultationPrice,
    ratingAvg = ratingAvg,
    ratingCount = ratingCount,
)

fun SpecialtyResultDto.toDomain() = SpecialtyResult(
    id = id,
    name = name,
    description = description,
    icon = icon,
)

fun AvailabilitySlotDto.toDomain() = AvailabilitySlot(
    time = time,
    available = available,
)

fun bookingResultDtoToDomain(dto: BookingResultDto): BookingResult =
    if (dto.ok) {
        BookingResult.Ok(
            appointmentId = requireNotNull(dto.appointmentId) { "ok=true but appointmentId is null" },
            paymentRedirectPath = dto.paymentRedirectPath ?: "/patient/payment/${dto.appointmentId}",
        )
    } else {
        BookingResult.Failed(
            errorCode = dto.error ?: "UNKNOWN",
            message = dto.message ?: "Error desconocido al agendar la cita",
        )
    }
