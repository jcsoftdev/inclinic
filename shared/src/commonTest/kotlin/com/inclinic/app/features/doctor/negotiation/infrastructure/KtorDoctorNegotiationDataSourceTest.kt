package com.inclinic.app.features.doctor.negotiation.infrastructure

import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.KtorDoctorNegotiationDataSource
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.dto.RespondNegotiationDto
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

class KtorDoctorNegotiationDataSourceTest {

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
    fun get_negotiation_returns_parsed_dto() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":{"id":"neg-1","patientName":"Patricia","packageName":"Diabetes","originalPriceCents":12000,"proposedPriceCents":10000,"message":"hola","status":"PENDING"}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorNegotiationDataSource(client, "https://api.test")
        val result = ds.getNegotiation("neg-1")
        assertTrue(result.isSuccess)
        assertEquals("neg-1", result.getOrThrow().id)
        assertEquals(10000, result.getOrThrow().proposedPriceCents)
    }

    @Test
    fun respond_negotiation_returns_accepted_dto() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":{"id":"neg-1","patientName":"Patricia","packageName":"Diabetes","originalPriceCents":12000,"proposedPriceCents":10000,"status":"ACCEPTED"}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorNegotiationDataSource(client, "https://api.test")
        val result = ds.respondNegotiation("neg-1", RespondNegotiationDto("ACCEPT", null))
        assertTrue(result.isSuccess)
        assertEquals("ACCEPTED", result.getOrThrow().status)
    }

    @Test
    fun respond_counter_returns_countered_dto() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":{"id":"neg-1","patientName":"Patricia","packageName":"Diabetes","originalPriceCents":12000,"proposedPriceCents":11000,"status":"COUNTERED"}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorNegotiationDataSource(client, "https://api.test")
        val result = ds.respondNegotiation("neg-1", RespondNegotiationDto("COUNTER", 11000))
        assertTrue(result.isSuccess)
        assertEquals("COUNTERED", result.getOrThrow().status)
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorDoctorNegotiationDataSource(client, "https://api.test")
        val result = ds.getNegotiation("neg-1")
        assertTrue(result.isFailure)
    }
}
