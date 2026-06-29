package com.inclinic.app.features.patient.assistant.infrastructure

import com.inclinic.app.features.patient.assistant.core.model.AssistantChatRequest
import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import com.inclinic.app.features.patient.assistant.core.port.AssistantChatDataSource
import com.inclinic.app.features.patient.assistant.infrastructure.dto.AssistantChatRequestDto
import com.inclinic.app.features.patient.assistant.infrastructure.parser.parseUIMessageChunk
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.IOException

/**
 * Ktor implementation of [AssistantChatDataSource].
 *
 * Streaming strategy: `preparePost().execute { call -> }` keeps the HTTP
 * connection alive for the duration of body draining. Each `data: {json}` line
 * is parsed via [parseUIMessageChunk] and emitted as an [AssistantStreamEvent].
 *
 * Error mapping (done before draining the body channel):
 *   401 → UNAUTHORIZED
 *   403 → FORBIDDEN
 *   422 → VALIDATION
 *   429 → RATE_LIMIT (+ Retry-After header, defaulting to 60 s)
 *   503 → DISABLED
 *   other 4xx/5xx → UNKNOWN
 *
 * The `x-conversation-id` header is read before the body channel is drained,
 * so [AssistantStreamEvent.ConversationIdReceived] is always the first emitted event.
 *
 * @param client An authenticated [HttpClient] injected by Koin (`named(APP_HTTP_CLIENT)`).
 *   No base URL is set on the client itself — pass [baseUrl] explicitly.
 * @param baseUrl Base URL without trailing slash (e.g. "http://10.0.2.2:3000").
 */
class KtorAssistantChatDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : AssistantChatDataSource {

    override suspend fun sendMessage(request: AssistantChatRequest): Flow<AssistantStreamEvent> = flow {
        try {
            val response: HttpResponse = client.post("$baseUrl/api/assistant/chat") {
                contentType(ContentType.Application.Json)
                setBody(AssistantChatRequestDto(request.message, request.conversationId))
            }

            val status = response.status.value

            if (status >= 400) {
                emit(mapHttpError(status, response.headers))
                return@flow
            }

            response.headers["x-conversation-id"]?.let { convId ->
                emit(AssistantStreamEvent.ConversationIdReceived(convId))
            }

            val channel: ByteReadChannel = response.body()
            while (!channel.isClosedForRead) {
                val line = channel.readLine() ?: break
                if (line.isBlank() || !line.startsWith("data: ")) continue
                parseUIMessageChunk(line.removePrefix("data: "))?.let { emit(it) }
            }
        } catch (io: IOException) {
            emit(AssistantStreamEvent.Error("NETWORK"))
        }
    }

    private fun mapHttpError(status: Int, headers: Headers): AssistantStreamEvent.Error = when (status) {
        401 -> AssistantStreamEvent.Error("UNAUTHORIZED")
        403 -> AssistantStreamEvent.Error("FORBIDDEN")
        422 -> AssistantStreamEvent.Error("VALIDATION")
        429 -> AssistantStreamEvent.Error(
            code = "RATE_LIMIT",
            retryAfterSeconds = headers["Retry-After"]?.toIntOrNull() ?: 60,
        )
        503 -> AssistantStreamEvent.Error("DISABLED")
        else -> AssistantStreamEvent.Error("UNKNOWN")
    }
}
