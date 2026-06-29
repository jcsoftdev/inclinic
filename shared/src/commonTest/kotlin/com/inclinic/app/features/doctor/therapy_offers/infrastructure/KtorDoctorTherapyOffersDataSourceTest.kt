package com.inclinic.app.features.doctor.therapy_offers.infrastructure

import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.KtorDoctorTherapyOffersDataSource
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.dto.CreateOfferRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorDoctorTherapyOffersDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient = HttpClient(MockEngine { handler(it) }) {
        install(ContentNegotiation) { json(json) }
    }

    @Test
    fun listMyOffers_hits_real_endpoint_and_parses_offers() = runTest {
        var capturedPath: String? = null
        val client = buildClient {
            capturedPath = it.url.encodedPath
            respond(
                content = """{"success":true,"data":[{"id":"o1","title":"Cardio Premium","specialtyId":"spe-1","totalSessions":8,"pricePerSession":80.0,"isActive":true,"specialty":{"id":"spe-1","name":"Cardiologia"}}]}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorTherapyOffersDataSource(client, "https://api.test")
        val result = ds.listMyOffers()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("o1", result.getOrThrow()[0].id)
        assertEquals("Cardiologia", result.getOrThrow()[0].specialty?.name)
        assertEquals("/api/doctors/me/therapy-offers", capturedPath)
    }

    @Test
    fun createOffer_posts_to_real_endpoint() = runTest {
        var capturedPath: String? = null
        var capturedMethod: HttpMethod? = null
        val client = buildClient {
            capturedPath = it.url.encodedPath
            capturedMethod = it.method
            respond(
                content = """{"success":true,"data":{"id":"new-o","title":"Plan Cardio","specialtyId":"spe-1","totalSessions":6,"pricePerSession":90.0,"isActive":true}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorTherapyOffersDataSource(client, "https://api.test")
        val body = CreateOfferRequestDto(
            specialtyId = "spe-1",
            title = "Plan Cardio",
            totalSessions = 6,
            pricePerSession = 90.0,
        )
        val result = ds.createOffer(body)
        assertTrue(result.isSuccess)
        assertEquals("new-o", result.getOrThrow().id)
        assertEquals("/api/therapy-offers", capturedPath)
        assertEquals(HttpMethod.Post, capturedMethod)
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorDoctorTherapyOffersDataSource(client, "https://api.test")
        val result = ds.listMyOffers()
        assertTrue(result.isFailure)
    }
}
