package com.inclinic.app.features.admin.infrastructure

import com.inclinic.app.features.admin.infrastructure.remote.KtorAdminDataSource
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KtorAdminExportTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://test.api.inclinic.com"

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        val engine = MockEngine { request -> handler(request) }
        return HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
    }

    @Test
    fun exportFinanceCsv_calls_GET_on_correct_endpoint() = runTest {
        var capturedPath: String? = null
        var capturedMethod: HttpMethod? = null
        var capturedQuery: String? = null
        val csvContent = "doctor,revenue\nDr. Smith,1000"
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            capturedMethod = request.method
            capturedQuery = request.url.encodedQuery
            respond(
                content = csvContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/csv"),
            )
        }
        val ds = KtorAdminDataSource(client, baseUrl)

        ds.exportFinanceCsv()

        assertEquals("/api/admin/finance/export", capturedPath)
        assertEquals(HttpMethod.Get, capturedMethod)
        assertNotNull(capturedQuery)
        assertTrue(
            capturedQuery!!.contains("format=csv"),
            "Expected format=csv in query, got: $capturedQuery"
        )
    }

    @Test
    fun exportFinanceCsv_returns_success_with_bytes_on_200() = runTest {
        val csvContent = "doctor,revenue\nDr. Smith,1000"
        val client = buildClient { _ ->
            respond(
                content = csvContent,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/csv"),
            )
        }
        val ds = KtorAdminDataSource(client, baseUrl)

        val result = ds.exportFinanceCsv()

        assertTrue(result.isSuccess)
        val bytes = result.getOrThrow()
        assertTrue(bytes.isNotEmpty())
    }

    @Test
    fun exportFinanceCsv_returns_failure_on_error_status() = runTest {
        val client = buildClient { _ ->
            respond(
                content = """{"success":false,"error":"Forbidden"}""",
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorAdminDataSource(client, baseUrl)

        val result = ds.exportFinanceCsv()

        assertTrue(result.isFailure)
    }
}
