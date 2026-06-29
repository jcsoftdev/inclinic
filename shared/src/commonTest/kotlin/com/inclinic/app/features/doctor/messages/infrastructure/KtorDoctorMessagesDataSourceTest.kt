package com.inclinic.app.features.doctor.messages.infrastructure

import com.inclinic.app.features.doctor.messages.infrastructure.remote.KtorDoctorMessagesDataSource
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

class KtorDoctorMessagesDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = HttpClient(MockEngine { handler(it) }) {
        install(ContentNegotiation) { json(json) }
    }

    @Test
    fun list_threads_hits_correct_endpoint() = runTest {
        var capturedUrl = ""
        val client = buildClient { req ->
            capturedUrl = req.url.toString()
            respond(content = """{"success":true,"data":[]}""", status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val ds = KtorDoctorMessagesDataSource(client, "https://api.test")
        ds.listThreads()
        assertTrue(capturedUrl.contains("/api/chats"))
    }

    @Test
    fun list_threads_returns_parsed_dtos() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":[{"id":"t1","otherPartyId":"p1","otherPartyName":"Juan Pérez","lastMessage":"Hola","lastAt":"1970-01-01T00:00:00Z","unread":true}]}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorMessagesDataSource(client, "https://api.test")
        val result = ds.listThreads()
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        assertEquals("t1", result.getOrThrow()[0].id)
        assertEquals("p1", result.getOrThrow()[0].otherPartyId)
        assertEquals("Juan Pérez", result.getOrThrow()[0].otherPartyName)
        assertTrue(result.getOrThrow()[0].unread)
    }

    @Test
    fun list_threads_returns_empty_on_empty_data() = runTest {
        val client = buildClient {
            respond(content = """{"success":true,"data":[]}""", status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val ds = KtorDoctorMessagesDataSource(client, "https://api.test")
        val result = ds.listThreads()
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorDoctorMessagesDataSource(client, "https://api.test")
        val result = ds.listThreads()
        assertTrue(result.isFailure)
    }
}
