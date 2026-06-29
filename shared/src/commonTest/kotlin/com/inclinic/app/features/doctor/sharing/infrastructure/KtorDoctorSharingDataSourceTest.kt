package com.inclinic.app.features.doctor.sharing.infrastructure

import com.inclinic.app.features.doctor.sharing.infrastructure.remote.KtorDoctorSharingDataSource
import com.inclinic.app.features.doctor.sharing.infrastructure.remote.dto.CreateShareRequestDto
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

class KtorDoctorSharingDataSourceTest {

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
    fun list_requests_hits_correct_endpoint() = runTest {
        var capturedUrl = ""
        val client = buildClient { req ->
            capturedUrl = req.url.toString()
            respond(
                content = """{"success":true,"data":[]}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorSharingDataSource(client, "https://api.test")
        ds.listRequests()
        assertTrue(capturedUrl.contains("/api/medical-history-share"))
    }

    @Test
    fun list_requests_returns_parsed_dtos() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":[{"id":"r1","patientId":"p1","requesterDoctorId":"d1","reason":"Seguimiento","scope":"FULL_HISTORY","status":"PENDING","createdAt":"1970-01-01T00:00:00Z"}]}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorSharingDataSource(client, "https://api.test")
        val result = ds.listRequests()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("r1", result.getOrThrow()[0].id)
        assertEquals("p1", result.getOrThrow()[0].patientId)
        assertEquals("PENDING", result.getOrThrow()[0].status)
    }

    @Test
    fun list_requests_returns_empty_on_empty_data() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":[]}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorSharingDataSource(client, "https://api.test")
        val result = ds.listRequests()
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }

    @Test
    fun request_share_posts_to_correct_endpoint() = runTest {
        var capturedUrl = ""
        var capturedMethod = HttpMethod.Get
        val client = buildClient { req ->
            capturedUrl = req.url.toString()
            capturedMethod = req.method
            respond(
                content = """{"success":true,"data":{"id":"new-1","patientId":"p1","requesterDoctorId":"d2","reason":"Seguimiento","scope":"FULL_HISTORY","status":"PENDING","createdAt":"1970-01-01T00:00:00Z"}}""",
                status = HttpStatusCode.Created,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorSharingDataSource(client, "https://api.test")
        val result = ds.requestShare(CreateShareRequestDto("p1", "Seguimiento médico del paciente", "FULL_HISTORY"))
        assertTrue(result.isSuccess)
        assertEquals("new-1", result.getOrThrow().id)
        assertTrue(capturedUrl.contains("/api/medical-history-share"))
        assertEquals(HttpMethod.Post, capturedMethod)
    }

    @Test
    fun cancel_request_sends_delete() = runTest {
        var capturedMethod = HttpMethod.Get
        var capturedUrl = ""
        val client = buildClient { req ->
            capturedMethod = req.method
            capturedUrl = req.url.toString()
            respond(
                content = """{"success":true,"data":{}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorSharingDataSource(client, "https://api.test")
        val result = ds.cancelRequest("req-42")
        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Delete, capturedMethod)
        assertTrue(capturedUrl.contains("/api/medical-history-share/req-42"))
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorDoctorSharingDataSource(client, "https://api.test")
        val result = ds.listRequests()
        assertTrue(result.isFailure)
    }
}
