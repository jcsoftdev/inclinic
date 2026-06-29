package com.inclinic.app.features.patient.assistant.infrastructure.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Loose DTO for a single `data: {json}` line from the Vercel AI SDK v6 UIMessageStream.
 *
 * All optional fields are null for chunk types that do not carry them; the
 * parser discards (returns null for) envelope-only chunk types.
 *
 * Field names verified against `node_modules/ai/dist/index.d.ts` UIMessageChunk type:
 *
 * | type                     | relevant fields                        |
 * |--------------------------|----------------------------------------|
 * | text-delta               | delta                                  |
 * | tool-input-available     | toolCallId, toolName, input            |
 * | tool-output-available    | toolCallId, output                     |
 * | finish                   | finishReason                           |
 * | error                    | errorText                              |
 * | start / start-step /     |                                        |
 * |   finish-step / text-start /                                      |
 * |   text-end / abort /     |                                        |
 * |   tool-input-start /     |                                        |
 * |   tool-input-delta       | (envelope — skipped by parser)         |
 *
 * Using `Json { ignoreUnknownKeys = true }` so any new envelope fields parse silently.
 */
@Serializable
data class UIMessageChunkDto(
    val type: String,
    // text-delta
    val delta: String? = null,
    // tool-input-available
    val toolCallId: String? = null,
    val toolName: String? = null,
    val input: JsonElement? = null,
    // tool-output-available
    val output: JsonElement? = null,
    // error
    val errorText: String? = null,
    // finish
    val finishReason: String? = null,
)
