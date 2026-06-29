package com.inclinic.app.features.patient.assistant.infrastructure.parser

import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import com.inclinic.app.features.patient.assistant.core.model.ToolName
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Unit tests for [parseUIMessageChunk].
 *
 * Each test exercises a single chunk type. The parser must return null (not throw)
 * for envelope-only chunks and malformed JSON, so callers can safely use
 * `parseUIMessageChunk(line)?.let { emit(it) }`.
 */
class UIMessageStreamParserTest {

    // ── text-delta ────────────────────────────────────────────────────────────

    @Test
    fun text_delta_chunk_returns_TextDelta_with_delta_text() {
        val json = """{"type":"text-delta","delta":"Hola, ¿en qué puedo ayudarte?"}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.TextDelta>(event)
        assertEquals("Hola, ¿en qué puedo ayudarte?", event.text)
    }

    @Test
    fun text_delta_with_empty_string_returns_TextDelta() {
        val json = """{"type":"text-delta","delta":""}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.TextDelta>(event)
        assertEquals("", event.text)
    }

    // ── tool-input-available ─────────────────────────────────────────────────

    @Test
    fun tool_input_available_searchDoctors_returns_ToolCallStarted() {
        val json = """{"type":"tool-input-available","toolCallId":"tc1","toolName":"searchDoctors","input":{"specialty":"cardiología"},"providerExecuted":false}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.ToolCallStarted>(event)
        assertEquals("tc1", event.toolCallId)
        assertEquals(ToolName.SEARCH_DOCTORS, event.toolName)
        val argsObj = assertIs<JsonObject>(event.args)
        assertEquals(JsonPrimitive("cardiología"), argsObj["specialty"])
    }

    @Test
    fun tool_input_available_listSpecialties_returns_ToolCallStarted() {
        val json = """{"type":"tool-input-available","toolCallId":"tc2","toolName":"listSpecialties","input":{}}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.ToolCallStarted>(event)
        assertEquals(ToolName.LIST_SPECIALTIES, event.toolName)
    }

    @Test
    fun tool_input_available_getAvailability_returns_ToolCallStarted() {
        val json = """{"type":"tool-input-available","toolCallId":"tc3","toolName":"getDoctorAvailability","input":{"doctorId":"d1"}}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.ToolCallStarted>(event)
        assertEquals(ToolName.GET_AVAILABILITY, event.toolName)
    }

    @Test
    fun tool_input_available_bookAppointment_returns_ToolCallStarted() {
        val json = """{"type":"tool-input-available","toolCallId":"tc4","toolName":"bookAppointment","input":{"doctorId":"d1","slot":"10:00"}}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.ToolCallStarted>(event)
        assertEquals(ToolName.BOOK_APPOINTMENT, event.toolName)
    }

    @Test
    fun tool_input_available_unknown_tool_name_returns_null() {
        // Unknown tool → ToolName.fromBackend returns null → parser returns null (skip)
        val json = """{"type":"tool-input-available","toolCallId":"tc5","toolName":"unknownTool","input":{}}"""
        val event = parseUIMessageChunk(json)
        assertNull(event)
    }

    // ── tool-output-available ─────────────────────────────────────────────────

    @Test
    fun tool_output_available_returns_ToolResult_with_raw_output() {
        val json = """{"type":"tool-output-available","toolCallId":"tc1","output":[{"id":"d1","name":"Dr. Ana Torres"}]}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.ToolResult>(event)
        assertEquals("tc1", event.toolCallId)
        // result is the raw JsonElement from the output field
        assertIs<kotlinx.serialization.json.JsonArray>(event.result)
    }

    // ── finish ────────────────────────────────────────────────────────────────

    @Test
    fun finish_chunk_returns_Finish() {
        val json = """{"type":"finish","finishReason":"stop"}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.Finish>(event)
    }

    @Test
    fun finish_chunk_without_finishReason_still_returns_Finish() {
        val json = """{"type":"finish"}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.Finish>(event)
    }

    // ── error ─────────────────────────────────────────────────────────────────

    @Test
    fun error_chunk_with_errorText_returns_Error_STREAM_ERROR() {
        val json = """{"type":"error","errorText":"Something went wrong on the server"}"""
        val event = parseUIMessageChunk(json)
        assertIs<AssistantStreamEvent.Error>(event)
        assertEquals("STREAM_ERROR", event.code)
    }

    // ── envelope chunks — all must return null ────────────────────────────────

    @Test
    fun start_chunk_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"start","messageId":"m1"}"""))
    }

    @Test
    fun start_step_chunk_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"start-step"}"""))
    }

    @Test
    fun finish_step_chunk_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"finish-step"}"""))
    }

    @Test
    fun text_start_chunk_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"text-start","id":"t1"}"""))
    }

    @Test
    fun text_end_chunk_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"text-end","id":"t1"}"""))
    }

    @Test
    fun abort_chunk_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"abort","reason":"user-cancelled"}"""))
    }

    @Test
    fun tool_input_start_chunk_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"tool-input-start","toolCallId":"tc1","toolName":"searchDoctors"}"""))
    }

    @Test
    fun tool_input_delta_chunk_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"tool-input-delta","toolCallId":"tc1","inputTextDelta":"{\"spec"}"""))
    }

    // ── malformed / unknown ───────────────────────────────────────────────────

    @Test
    fun malformed_json_returns_null() {
        assertNull(parseUIMessageChunk("{not valid json"))
    }

    @Test
    fun unknown_type_returns_null() {
        assertNull(parseUIMessageChunk("""{"type":"some-future-chunk","data":"x"}"""))
    }

    @Test
    fun completely_empty_json_object_returns_null() {
        // type field is empty string → falls through to else → null
        assertNull(parseUIMessageChunk("{}"))
    }
}
