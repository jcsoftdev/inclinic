package com.inclinic.app.features.doctor.modality.infrastructure

import com.inclinic.app.features.doctor.modality.infrastructure.remote.KtorModalityRequestDataSource
import com.inclinic.app.features.doctor.modality.infrastructure.remote.dto.RespondModalityChangeDto
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

class KtorModalityRequestDataSourceTest {

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
    fun get_request_returns_parsed_dto() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":{"id":"req-1","patientName":"Luis","appointmentSlot":"Mar 18 · 11:30","currentModality":"Oficina","requestedModality":"Domicilio","reason":"Movilidad","status":"PENDING"}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorModalityRequestDataSource(client, "https://api.test")
        val result = ds.getRequest("req-1")
        assertTrue(result.isSuccess)
        assertEquals("req-1", result.getOrThrow().id)
        assertEquals("Domicilio", result.getOrThrow().requestedModality)
    }

    @Test
    fun respond_returns_approved_dto() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":{"id":"req-1","patientName":"Luis","appointmentSlot":"Mar 18 · 11:30","currentModality":"Oficina","requestedModality":"Domicilio","reason":null,"status":"APPROVED"}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorModalityRequestDataSource(client, "https://api.test")
        val result = ds.respond("req-1", RespondModalityChangeDto("APPROVE"))
        assertTrue(result.isSuccess)
        assertEquals("APPROVED", result.getOrThrow().status)
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorModalityRequestDataSource(client, "https://api.test")
        val result = ds.getRequest("req-1")
        assertTrue(result.isFailure)
    }
}
