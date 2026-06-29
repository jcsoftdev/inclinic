package com.inclinic.app.features.patient.assistant.infrastructure.parser

import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import com.inclinic.app.features.patient.assistant.infrastructure.dto.UIMessageChunkDto
import com.inclinic.app.features.patient.assistant.infrastructure.dto.toStreamEvent
import kotlinx.serialization.json.Json

/**
 * JSON parser configuration for UIMessageStream lines.
 * `ignoreUnknownKeys = true` ensures additive backend changes (new envelope fields,
 * new chunk sub-types) parse silently without crashing the stream.
 */
private val streamJson = Json { ignoreUnknownKeys = true }

/**
 * Pure top-level function: parses a single `data: {json}` line (already stripped of the
 * `data: ` prefix) into a domain [AssistantStreamEvent], or returns **null** when:
 *
 * - The JSON is malformed
 * - The chunk type is an envelope-only type (start, finish-step, text-start, etc.)
 * - A required field is absent (e.g., unknown tool name, missing toolCallId)
 *
 * Callers can safely use `parseUIMessageChunk(line)?.let { emit(it) }`.
 */
fun parseUIMessageChunk(json: String): AssistantStreamEvent? =
    runCatching {
        val dto = streamJson.decodeFromString<UIMessageChunkDto>(json)
        dto.toStreamEvent()
    }.getOrNull()
