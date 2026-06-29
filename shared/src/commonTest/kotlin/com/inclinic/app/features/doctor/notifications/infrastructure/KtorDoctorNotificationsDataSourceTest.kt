package com.inclinic.app.features.doctor.notifications.infrastructure

import com.inclinic.app.features.doctor.notifications.infrastructure.remote.KtorDoctorNotificationsDataSource
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

class KtorDoctorNotificationsDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = HttpClient(MockEngine { handler(it) }) {
        install(ContentNegotiation) { json(json) }
    }

    @Test
    fun list_hits_correct_endpoint() = runTest {
        var capturedUrl = ""
        val client = buildClient { req ->
            capturedUrl = req.url.toString()
            respond(
                content = """{"success":true,"data":{"items":[],"unreadCount":0}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorNotificationsDataSource(client, "https://api.test")
        ds.list()
        assertTrue(capturedUrl.contains("/api/notifications"))
    }

    @Test
    fun list_returns_parsed_notifications_with_type_field() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"items":[{"id":"n1","type":"APPOINTMENT_CONFIRMED","title":"Cita","body":"Mañana","isRead":false,"createdAt":"1970-01-01T00:00:00Z"}],"unreadCount":1}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorNotificationsDataSource(client, "https://api.test")
        val result = ds.list()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().items.size)
        assertEquals("n1", result.getOrThrow().items[0].id)
        assertEquals("APPOINTMENT_CONFIRMED", result.getOrThrow().items[0].type)
        assertEquals(1, result.getOrThrow().unreadCount)
    }

    @Test
    fun list_returns_empty_items_when_data_empty() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":{"items":[],"unreadCount":0}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorNotificationsDataSource(client, "https://api.test")
        val result = ds.list()
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow().items)
    }

    @Test
    fun mark_read_sends_put_to_correct_endpoint() = runTest {
        var capturedMethod = HttpMethod.Get
        var capturedUrl = ""
        val client = buildClient { req ->
            capturedMethod = req.method
            capturedUrl = req.url.toString()
            respond(content = """{"success":true,"data":{}}""", status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val ds = KtorDoctorNotificationsDataSource(client, "https://api.test")
        val result = ds.markRead("n1")
        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Put, capturedMethod)
        assertTrue(capturedUrl.contains("/api/notifications/n1"))
    }

    @Test
    fun mark_read_fails_on_error_status() = runTest {
        val client = buildClient {
            respond(content = """{"error":"not found"}""", status = HttpStatusCode.NotFound, headers = jsonHeaders)
        }
        val ds = KtorDoctorNotificationsDataSource(client, "https://api.test")
        val result = ds.markRead("bad-id")
        assertTrue(result.isFailure)
    }

    @Test
    fun mark_all_read_posts_to_correct_endpoint() = runTest {
        var capturedMethod = HttpMethod.Get
        var capturedUrl = ""
        val client = buildClient { req ->
            capturedMethod = req.method
            capturedUrl = req.url.toString()
            respond(content = """{"success":true,"data":{}}""", status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val ds = KtorDoctorNotificationsDataSource(client, "https://api.test")
        val result = ds.markAllRead()
        assertTrue(result.isSuccess)
        assertEquals(HttpMethod.Post, capturedMethod)
        assertTrue(capturedUrl.contains("/api/notifications/mark-all-read"))
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorDoctorNotificationsDataSource(client, "https://api.test")
        val result = ds.list()
        assertTrue(result.isFailure)
    }
}
