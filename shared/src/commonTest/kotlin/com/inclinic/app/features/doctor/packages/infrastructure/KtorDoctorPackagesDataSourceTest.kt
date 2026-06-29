package com.inclinic.app.features.doctor.packages.infrastructure

import com.inclinic.app.features.doctor.packages.infrastructure.remote.KtorDoctorPackagesDataSource
import com.inclinic.app.features.doctor.packages.infrastructure.remote.dto.CreatePackageRequestDto
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
import io.ktor.serialization.kotlinx.json.json as ktorJson
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorDoctorPackagesDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient = HttpClient(MockEngine { handler(it) }) {
        install(ContentNegotiation) { ktorJson(json) }
    }

    // ── list ──────────────────────────────────────────────────────────────────

    @Test
    fun list_hits_real_endpoint_and_parses_packages() = runTest {
        var capturedPath: String? = null
        val client = buildClient {
            capturedPath = it.url.encodedPath
            respond(
                content = """{"success":true,"data":[
                    {"id":"pkg-1","patientId":"pat-1","specialtyId":"spe-1","packageName":"Cardio Premium",
                     "totalSessions":10,"regularPricePerSession":150.0,"packagePricePerSession":120.0,
                     "isPrepaid":true,"prepaidDiscount":15.0,"totalPrepaidAmount":1020.0,
                     "sessionsCompleted":1,"sessionsScheduled":1,"sessionsUsed":1,"status":"ACTIVE",
                     "patient":{"user":{"firstName":"Roberto","lastName":"Valdez","email":"r.valdez@gmail.com"}},
                     "specialty":{"name":"Cardiología"},
                     "sessions":[{"id":"s1","status":"COMPLETED","sessionNumber":5}]}
                ]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorPackagesDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.list()

        assertTrue(result.isSuccess, "expected success, got ${result.exceptionOrNull()}")
        val packages = result.getOrThrow()
        assertEquals(1, packages.size)
        assertEquals("pkg-1", packages[0].id)
        assertEquals("Cardio Premium", packages[0].packageName)
        assertEquals(120.0, packages[0].packagePricePerSession)
        assertEquals("/api/therapy-packages", capturedPath)
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    fun create_posts_to_real_endpoint_and_returns_created_package() = runTest {
        var capturedPath: String? = null
        var capturedMethod: HttpMethod? = null
        val client = buildClient {
            capturedPath = it.url.encodedPath
            capturedMethod = it.method
            respond(
                content = """{"success":true,"data":{"id":"new-id","patientId":"pat-1","specialtyId":"spe-1",
                    "packageName":"Plan nutricional","totalSessions":12,"regularPricePerSession":70.0,
                    "packagePricePerSession":60.0,"isPrepaid":true,"prepaidDiscount":15.0,"status":"PENDING_PAYMENT"}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorPackagesDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val request = CreatePackageRequestDto(
            patientId = "pat-1",
            specialtyId = "spe-1",
            packageName = "Plan nutricional",
            totalSessions = 12,
            regularPricePerSession = 70.0,
            packagePricePerSession = 60.0,
            isPrepaid = true,
            prepaidDiscount = 15.0,
            isHomeVisit = false,
        )

        val result = ds.create(request)

        assertTrue(result.isSuccess)
        assertEquals("new-id", result.getOrThrow().id)
        assertEquals("/api/therapy-packages", capturedPath)
        assertEquals(HttpMethod.Post, capturedMethod)
    }

    // ── cancel ──────────────────────────────────────────────────────────────────

    @Test
    fun cancel_deletes_real_endpoint_with_id() = runTest {
        var capturedPath: String? = null
        var capturedMethod: HttpMethod? = null
        val client = buildClient {
            capturedPath = it.url.encodedPath
            capturedMethod = it.method
            respond(
                content = """{"success":true,"data":{}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorPackagesDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.cancel("pkg-99")

        assertTrue(result.isSuccess)
        assertEquals("/api/therapy-packages/pkg-99", capturedPath)
        assertEquals(HttpMethod.Delete, capturedMethod)
    }
}
