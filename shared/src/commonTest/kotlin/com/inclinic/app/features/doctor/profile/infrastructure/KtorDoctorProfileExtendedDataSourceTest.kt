package com.inclinic.app.features.doctor.profile.infrastructure

import com.inclinic.app.features.doctor.profile.infrastructure.remote.KtorDoctorProfileExtendedDataSource
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KtorDoctorProfileExtendedDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        val engine = MockEngine { request -> handler(request) }
        return HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
    }

    private val baseUrl = "https://test.api.inclinic.com"
    private val doctorId = "doc-123"

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    fun getProfile_returns_profile_on_success() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"id":"doc-1","bio":"Bio","licenseNumber":"CMP-1","user":{"firstName":"Carlos","lastName":"Ramos","email":"r@test.com"}}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.getProfile(doctorId)

        assertTrue(result.isSuccess)
        assertEquals("doc-1", result.getOrThrow().id)
    }

    @Test
    fun getProfile_calls_correct_endpoint() = runTest {
        var capturedPath: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            respond(
                content = """{"success":true,"data":{"id":"doc-1","user":{"firstName":"A","lastName":"B","email":"a@b.com"}}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        ds.getProfile(doctorId)

        assertNotNull(capturedPath)
        // Verify real backend path — NOT the old /api/v1/doctor/me/profile
        assertTrue(
            capturedPath!!.contains("/api/doctors/$doctorId"),
            "Expected path /api/doctors/$doctorId, got: $capturedPath"
        )
        assertTrue(
            !capturedPath!!.contains("v1"),
            "Path must NOT contain /v1, got: $capturedPath"
        )
    }

    // ── editSpecialties ───────────────────────────────────────────────────────

    @Test
    fun editSpecialties_calls_correct_endpoint() = runTest {
        var capturedPath: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.editSpecialties(doctorId, listOf("sp-1", "sp-2"))

        assertTrue(result.isSuccess)
        assertTrue(
            capturedPath!!.contains("/api/doctors/$doctorId/specialties"),
            "Expected path /api/doctors/$doctorId/specialties, got: $capturedPath"
        )
        assertTrue(
            !capturedPath!!.contains("v1"),
            "Path must NOT contain /v1, got: $capturedPath"
        )
    }

    // ── getMetrics (income) ───────────────────────────────────────────────────

    @Test
    fun getMetrics_returns_income_data_on_success() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"monthRevenue":{"amount":1800.0,"commission":270.0,"net":1530.0,"sessions":6,"growthPct":12.0}}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.getMetrics()

        assertTrue(result.isSuccess)
        val metrics = result.getOrThrow()
        assertEquals(1800.0, metrics.monthRevenue?.amount)
        assertEquals(6, metrics.monthRevenue?.sessions)
    }

    @Test
    fun getMetrics_calls_correct_endpoint() = runTest {
        var capturedPath: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            respond(
                content = """{"success":true,"data":{}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        ds.getMetrics()

        assertNotNull(capturedPath)
        assertTrue(
            capturedPath!!.contains("/api/doctors/me/metrics"),
            "Expected path /api/doctors/me/metrics, got: $capturedPath"
        )
    }

    // ── getReviews ────────────────────────────────────────────────────────────

    @Test
    fun getMetrics_parses_breakdown_when_present() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"monthRevenue":{"amount":1800.0,"commission":270.0,"net":1530.0,"sessions":6,"growthPct":12.0,"breakdown":{"retained":500.0,"released":1000.0,"refunded":300.0}}}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.getMetrics()

        assertTrue(result.isSuccess)
        val breakdown = result.getOrThrow().monthRevenue?.breakdown
        assertNotNull(breakdown)
        assertEquals(500.0, breakdown.retained)
        assertEquals(1000.0, breakdown.released)
        assertEquals(300.0, breakdown.refunded)
    }

    @Test
    fun getMetrics_returns_null_breakdown_when_absent_for_backward_compat() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"monthRevenue":{"amount":1800.0,"commission":270.0,"net":1530.0,"sessions":6}}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.getMetrics()

        assertTrue(result.isSuccess)
        // breakdown is optional — absence should not cause error
        val breakdown = result.getOrThrow().monthRevenue?.breakdown
        // null is acceptable for backward compatibility
        assertTrue(breakdown == null || breakdown.retained == 0.0)
    }

    @Test
    fun getReviews_returns_reviews_on_success() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"averageRating":4.8,"totalRatings":2,"reviews":[{"id":"r1","rating":5,"patientInitials":"A. R."}]}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.getReviews(doctorId, 20)

        assertTrue(result.isSuccess)
        val dto = result.getOrThrow()
        assertEquals(4.8, dto.averageRating)
        assertEquals(1, dto.reviews.size)
    }

    @Test
    fun getReviews_calls_correct_endpoint() = runTest {
        var capturedPath: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            respond(
                content = """{"success":true,"data":{"averageRating":0.0,"totalRatings":0,"reviews":[]}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        ds.getReviews(doctorId, 20)

        assertNotNull(capturedPath)
        assertTrue(
            capturedPath!!.contains("/api/doctors/$doctorId/reviews"),
            "Expected path /api/doctors/$doctorId/reviews, got: $capturedPath"
        )
    }
}
