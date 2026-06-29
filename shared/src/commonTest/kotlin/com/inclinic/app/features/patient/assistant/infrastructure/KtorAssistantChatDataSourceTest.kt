package com.inclinic.app.features.patient.assistant.infrastructure

import app.cash.turbine.test
import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import com.inclinic.app.features.patient.assistant.core.model.AssistantChatRequest
import com.inclinic.app.features.patient.assistant.core.model.ToolName
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for [KtorAssistantChatDataSource] using Ktor [MockEngine].
 * Turbine [test] is used for Flow assertions.
 */
class KtorAssistantChatDataSourceTest {

    private val baseUrl = "http://test.api.inclinic.com"
    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(handler: suspend io.ktor.client.engine.mock.MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> io.ktor.client.request.HttpResponseData): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) }) {
            install(ContentNegotiation) { json(json) }
        }
    }

    private fun sseBody(vararg lines: String): ByteReadChannel {
        val body = lines.joinToString("\n") { "data: $it" } + "\n"
        return ByteReadChannel(body)
    }

    private fun defaultHeaders(conversationId: String? = "conv-abc"): Headers =
        headersOf(
            HttpHeaders.ContentType to listOf("text/plain; charset=utf-8"),
            *(if (conversationId != null)
                arrayOf("x-conversation-id" to listOf(conversationId))
            else emptyArray()),
        )

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    fun happy_path_emits_events_in_order() = runTest {
        val client = buildClient {
            respond(
                content = sseBody(
                    """{"type":"text-delta","delta":"Hola"}""",
                    """{"type":"tool-input-available","toolCallId":"tc1","toolName":"searchDoctors","input":{"specialty":"cardiología"}}""",
                    """{"type":"tool-output-available","toolCallId":"tc1","output":[{"id":"d1","name":"Dr. Ana"}]}""",
                    """{"type":"finish","finishReason":"stop"}""",
                ),
                status = HttpStatusCode.OK,
                headers = defaultHeaders("conv-123"),
            )
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("Hola")).test {
            val id = awaitItem()
            assertIs<AssistantStreamEvent.ConversationIdReceived>(id)
            assertEquals("conv-123", id.conversationId)

            val delta = awaitItem()
            assertIs<AssistantStreamEvent.TextDelta>(delta)
            assertEquals("Hola", delta.text)

            val toolCall = awaitItem()
            assertIs<AssistantStreamEvent.ToolCallStarted>(toolCall)
            assertEquals("tc1", toolCall.toolCallId)
            assertEquals(ToolName.SEARCH_DOCTORS, toolCall.toolName)

            val toolResult = awaitItem()
            assertIs<AssistantStreamEvent.ToolResult>(toolResult)
            assertEquals("tc1", toolResult.toolCallId)

            val finish = awaitItem()
            assertIs<AssistantStreamEvent.Finish>(finish)

            awaitComplete()
        }
    }

    @Test
    fun no_conversation_id_header_does_not_emit_ConversationIdReceived() = runTest {
        val client = buildClient {
            respond(
                content = sseBody("""{"type":"finish","finishReason":"stop"}"""),
                status = HttpStatusCode.OK,
                headers = defaultHeaders(conversationId = null),
            )
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            val finish = awaitItem()
            assertIs<AssistantStreamEvent.Finish>(finish)
            awaitComplete()
        }
    }

    // ── HTTP error codes ──────────────────────────────────────────────────────

    @Test
    fun status_401_emits_Error_UNAUTHORIZED() = runTest {
        val client = buildClient {
            respond("", HttpStatusCode.Unauthorized, headersOf())
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            val err = awaitItem()
            assertIs<AssistantStreamEvent.Error>(err)
            assertEquals("UNAUTHORIZED", err.code)
            awaitComplete()
        }
    }

    @Test
    fun status_403_emits_Error_FORBIDDEN() = runTest {
        val client = buildClient {
            respond("", HttpStatusCode.Forbidden, headersOf())
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            val err = awaitItem()
            assertIs<AssistantStreamEvent.Error>(err)
            assertEquals("FORBIDDEN", err.code)
            awaitComplete()
        }
    }

    @Test
    fun status_422_emits_Error_VALIDATION() = runTest {
        val client = buildClient {
            respond("", HttpStatusCode.UnprocessableEntity, headersOf())
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            val err = awaitItem()
            assertIs<AssistantStreamEvent.Error>(err)
            assertEquals("VALIDATION", err.code)
            awaitComplete()
        }
    }

    @Test
    fun status_429_with_retry_after_header_emits_RATE_LIMIT_with_seconds() = runTest {
        val client = buildClient {
            respond(
                content = "",
                status = HttpStatusCode.TooManyRequests,
                headers = headersOf("Retry-After" to listOf("30")),
            )
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            val err = awaitItem()
            assertIs<AssistantStreamEvent.Error>(err)
            assertEquals("RATE_LIMIT", err.code)
            assertEquals(30, err.retryAfterSeconds)
            awaitComplete()
        }
    }

    @Test
    fun status_429_without_retry_after_defaults_to_60_seconds() = runTest {
        val client = buildClient {
            respond("", HttpStatusCode.TooManyRequests, headersOf())
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            val err = awaitItem()
            assertIs<AssistantStreamEvent.Error>(err)
            assertEquals("RATE_LIMIT", err.code)
            assertEquals(60, err.retryAfterSeconds)
            awaitComplete()
        }
    }

    @Test
    fun status_503_emits_Error_DISABLED() = runTest {
        val client = buildClient {
            respond("", HttpStatusCode.ServiceUnavailable, headersOf())
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            val err = awaitItem()
            assertIs<AssistantStreamEvent.Error>(err)
            assertEquals("DISABLED", err.code)
            awaitComplete()
        }
    }

    // ── Malformed / blank lines ───────────────────────────────────────────────

    @Test
    fun malformed_chunk_line_is_skipped_valid_chunks_still_emit() = runTest {
        val client = buildClient {
            respond(
                content = sseBody(
                    """{"type":"text-delta","delta":"before"}""",
                    """{"not valid json""",          // malformed — skipped
                    """{"type":"finish","finishReason":"stop"}""",
                ),
                status = HttpStatusCode.OK,
                headers = defaultHeaders(),
            )
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            assertIs<AssistantStreamEvent.ConversationIdReceived>(awaitItem())
            val delta = awaitItem()
            assertIs<AssistantStreamEvent.TextDelta>(delta)
            assertEquals("before", delta.text)
            assertIs<AssistantStreamEvent.Finish>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun blank_lines_are_skipped() = runTest {
        // SSE streams include blank separator lines between events
        val body = "data: {\"type\":\"text-delta\",\"delta\":\"hi\"}\n\ndata: {\"type\":\"finish\",\"finishReason\":\"stop\"}\n"
        val client = buildClient {
            respond(
                content = ByteReadChannel(body),
                status = HttpStatusCode.OK,
                headers = defaultHeaders(),
            )
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            assertIs<AssistantStreamEvent.ConversationIdReceived>(awaitItem())
            assertIs<AssistantStreamEvent.TextDelta>(awaitItem())
            assertIs<AssistantStreamEvent.Finish>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun lines_not_starting_with_data_prefix_are_skipped() = runTest {
        // SSE can include comment lines ("event: ...", "id: ...", ": comment")
        val body = ": this is a comment\nevent: message\ndata: {\"type\":\"finish\",\"finishReason\":\"stop\"}\n"
        val client = buildClient {
            respond(
                content = ByteReadChannel(body),
                status = HttpStatusCode.OK,
                headers = defaultHeaders(),
            )
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            assertIs<AssistantStreamEvent.ConversationIdReceived>(awaitItem())
            assertIs<AssistantStreamEvent.Finish>(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun envelope_chunks_are_skipped_silently() = runTest {
        val client = buildClient {
            respond(
                content = sseBody(
                    """{"type":"start","messageId":"m1"}""",
                    """{"type":"start-step"}""",
                    """{"type":"text-start","id":"t1"}""",
                    """{"type":"text-delta","delta":"chunk"}""",
                    """{"type":"text-end","id":"t1"}""",
                    """{"type":"finish-step"}""",
                    """{"type":"finish","finishReason":"stop"}""",
                ),
                status = HttpStatusCode.OK,
                headers = defaultHeaders(),
            )
        }
        val dataSource = KtorAssistantChatDataSource(client, baseUrl)

        dataSource.sendMessage(AssistantChatRequest("test")).test {
            assertIs<AssistantStreamEvent.ConversationIdReceived>(awaitItem())
            val delta = awaitItem()
            assertIs<AssistantStreamEvent.TextDelta>(delta)
            assertEquals("chunk", delta.text)
            assertIs<AssistantStreamEvent.Finish>(awaitItem())
            awaitComplete()
        }
    }
}
