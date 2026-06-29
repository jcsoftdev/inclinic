package com.inclinic.app.core.upload

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * RED → GREEN tests for [KtorUploadDataSource].
 *
 * Uses Ktor [MockEngine] — no network required.
 *
 * TDD cycle:
 *   RED   — KtorUploadDataSource does not exist; tests fail to compile.
 *   GREEN — KtorUploadDataSource posts multipart/form-data to /api/upload
 *           and deserialises the server envelope.
 */
class KtorUploadDataSourceTest {

    private val baseUrl = "http://test.api.inclinic.com"
    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(responder: suspend io.ktor.client.engine.mock.MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> io.ktor.client.request.HttpResponseData): HttpClient =
        HttpClient(MockEngine { req -> responder(req) }) {
            install(ContentNegotiation) { json(json) }
        }

    // ── Happy path ─────────────────────────────────────────────────────────────

    @Test
    fun happy_path_returns_upload_result_dto() = runTest {
        val responseJson = """
            {
              "success": true,
              "data": {
                "url":    "https://cdn.inclinic.com/documents/cert.pdf",
                "path":   "documents/cert.pdf",
                "bucket": "documents",
                "size":   4096,
                "type":   "application/pdf"
              }
            }
        """.trimIndent()

        val client = buildClient {
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
            )
        }

        val ds = KtorUploadDataSource(client = client, baseUrl = baseUrl)
        val result = ds.upload(
            bucket = "documents",
            bytes = byteArrayOf(1, 2, 3),
            fileName = "cert.pdf",
            mimeType = "application/pdf",
        )

        assertTrue(result.isSuccess)
        val dto = result.getOrThrow()
        assertEquals("https://cdn.inclinic.com/documents/cert.pdf", dto.url)
        assertEquals("documents", dto.bucket)
        assertEquals(4096L, dto.size)
    }

    @Test
    fun posts_to_correct_endpoint() = runTest {
        var capturedPath = ""
        val client = buildClient { req ->
            capturedPath = req.url.encodedPath
            respond(
                content = """{"success":true,"data":{"url":"u","path":"p","bucket":"b","size":1,"type":"t"}}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
            )
        }

        val ds = KtorUploadDataSource(client = client, baseUrl = baseUrl)
        ds.upload("medical-attachments", byteArrayOf(1), "photo.jpg", "image/jpeg")

        assertEquals("/api/upload", capturedPath)
    }

    @Test
    fun server_error_returns_failure() = runTest {
        val client = buildClient {
            respond(
                content = """{"error":"File too large","code":"FILE_TOO_LARGE"}""",
                status = HttpStatusCode.PayloadTooLarge,
                headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
            )
        }

        val ds = KtorUploadDataSource(client = client, baseUrl = baseUrl)
        val result = ds.upload("documents", byteArrayOf(1), "huge.pdf", "application/pdf")

        assertTrue(result.isFailure)
    }
}
