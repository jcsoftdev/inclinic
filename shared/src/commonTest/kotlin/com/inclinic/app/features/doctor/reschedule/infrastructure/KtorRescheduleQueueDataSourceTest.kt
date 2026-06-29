package com.inclinic.app.features.doctor.reschedule.infrastructure

import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.KtorRescheduleQueueDataSource
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RespondRescheduleRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorRescheduleQueueDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        return HttpClient(MockEngine { handler(it) }) {
            install(ContentNegotiation) { json(json) }
        }
    }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    @Test
    fun list_requests_returns_parsed_dtos() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":[{"id":"r1","patientName":"María","currentSlot":"Mar 15 · 10:00","requestedSlot":"Mar 17 · 14:00","reason":"viaje","status":"PENDING"}]}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorRescheduleQueueDataSource(client, "https://api.test")
        val result = ds.listRequests()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("r1", result.getOrThrow()[0].id)
    }

    @Test
    fun list_requests_returns_empty_on_empty_data() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":[]}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorRescheduleQueueDataSource(client, "https://api.test")
        val result = ds.listRequests()
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }

    @Test
    fun respond_returns_approved_dto() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":{"id":"req-1","patientName":"María","currentSlot":"Mar 15 · 10:00","requestedSlot":"Mar 17 · 14:00","reason":null,"status":"APPROVED"}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorRescheduleQueueDataSource(client, "https://api.test")
        val result = ds.respond("req-1", RespondRescheduleRequestDto("APPROVE"))
        assertTrue(result.isSuccess)
        assertEquals("APPROVED", result.getOrThrow().status)
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorRescheduleQueueDataSource(client, "https://api.test")
        val result = ds.listRequests()
        assertTrue(result.isFailure)
    }
}
