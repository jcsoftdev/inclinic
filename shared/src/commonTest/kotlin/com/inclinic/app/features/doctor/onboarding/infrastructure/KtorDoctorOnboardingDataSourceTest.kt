package com.inclinic.app.features.doctor.onboarding.infrastructure

import com.inclinic.app.features.doctor.onboarding.core.model.DocKind
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.core.model.PersonalData
import com.inclinic.app.features.doctor.onboarding.core.model.PriceConfig
import com.inclinic.app.features.doctor.onboarding.core.model.WeeklySchedule
import com.inclinic.app.features.doctor.onboarding.infrastructure.remote.KtorDoctorOnboardingDataSource
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

class KtorDoctorOnboardingDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        val engine = MockEngine { request -> handler(request) }
        return HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
    }

    // ── getStatus ─────────────────────────────────────────────────────────────

    @Test
    fun getStatus_returns_PENDING_from_json_response() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"status":"PENDING"}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorOnboardingDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.getStatus()

        assertTrue(result.isSuccess)
        assertEquals("PENDING", result.getOrThrow().status)
    }

    @Test
    fun getStatus_returns_APPROVED_from_json_response() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"status":"APPROVED"}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorOnboardingDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.getStatus()

        assertTrue(result.isSuccess)
        assertEquals("APPROVED", result.getOrThrow().status)
    }

    // ── submit ────────────────────────────────────────────────────────────────

    @Test
    fun submit_posts_and_returns_success_on_202() = runTest {
        var capturedPath: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            respond(
                content = "",
                status = HttpStatusCode.Accepted,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorOnboardingDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val draft = DoctorOnboardingDraft(
            personalData = PersonalData(
                firstName = "Carlos",
                lastName = "Ramirez",
                cmpLicense = "CMP-12345",
                phone = "+51999000111",
            ),
            documents = emptyList(),
            specialties = listOf("sp-cardiology"),
            schedule = WeeklySchedule(slots = emptyMap()),
            prices = PriceConfig(consultationFee = 80.0, supportsPresential = true, supportsVirtual = false),
        )

        val result = ds.submit(draft)

        assertTrue(result.isSuccess)
        assertNotNull(capturedPath)
        assertTrue(capturedPath!!.contains("onboarding/submit"), "Expected path to contain 'onboarding/submit', got: $capturedPath")
    }

    // ── resubmit ──────────────────────────────────────────────────────────────

    @Test
    fun resubmit_posts_corrections_map_and_returns_success() = runTest {
        var capturedPath: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            respond(
                content = "",
                status = HttpStatusCode.Accepted,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorOnboardingDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.resubmit(mapOf("cmpLicense" to "CMP-99999"))

        assertTrue(result.isSuccess)
        assertTrue(capturedPath!!.contains("onboarding/resubmit"), "Expected path to contain 'onboarding/resubmit', got: $capturedPath")
    }

    // ── uploadDocument ────────────────────────────────────────────────────────

    @Test
    fun uploadDocument_returns_uploaded_doc_on_success() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"id":"doc-1","kind":"CMP_LICENSE","url":"https://cdn.inclinic.com/doc-1"}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorOnboardingDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.uploadDocument(
            file = byteArrayOf(1, 2, 3),
            fileName = "cmp.pdf",
            kind = DocKind.CMP_LICENSE,
        )

        assertTrue(result.isSuccess)
        val doc = result.getOrThrow()
        assertEquals("doc-1", doc.id)
        assertEquals("CMP_LICENSE", doc.kind)
        assertEquals("https://cdn.inclinic.com/doc-1", doc.url)
    }
}
