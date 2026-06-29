package com.inclinic.app.features.doctor.settings.infrastructure

import com.inclinic.app.features.doctor.settings.infrastructure.remote.KtorDoctorSettingsDataSource
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

class KtorDoctorSettingsDataSourceTest {

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

    // ── getMercadoPagoConnectUrl ──────────────────────────────────────────────

    @Test
    fun getMercadoPagoConnectUrl_calls_GET_on_correct_endpoint() = runTest {
        var capturedPath: String? = null
        var capturedMethod: HttpMethod? = null

        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            capturedMethod = request.method
            respond(
                content = """{"success":true,"data":{"url":"https://mp.oauth.test/auth"}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorSettingsDataSource(client, baseUrl)

        ds.getMercadoPagoConnectUrl()

        assertEquals("/api/doctors/me/mercadopago/connect", capturedPath)
        assertEquals(HttpMethod.Get, capturedMethod)
    }

    @Test
    fun getMercadoPagoConnectUrl_returns_url_string_on_success() = runTest {
        val expectedUrl = "https://auth.mercadopago.com.ar/authorization?response_type=code&client_id=123"
        val client = buildClient { _ ->
            respond(
                content = """{"success":true,"data":{"url":"$expectedUrl"}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorSettingsDataSource(client, baseUrl)

        val result = ds.getMercadoPagoConnectUrl()

        assertTrue(result.isSuccess)
        assertEquals(expectedUrl, result.getOrThrow())
    }

    @Test
    fun getMercadoPagoConnectUrl_fails_with_mp_not_configured_message_on_503() = runTest {
        val client = buildClient { _ ->
            respond(
                content = """{"success":false,"code":"MP_NOT_CONFIGURED","error":"MercadoPago not configured"}""",
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorSettingsDataSource(client, baseUrl)

        val result = ds.getMercadoPagoConnectUrl()

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message ?: ""
        assertTrue(
            msg.contains("MP_NOT_CONFIGURED") || msg.contains("no configurada") || msg.contains("not configured", ignoreCase = true),
            "Expected MP_NOT_CONFIGURED signal in message, got: $msg",
        )
    }

    // ── disconnectMercadoPago ─────────────────────────────────────────────────

    @Test
    fun disconnectMercadoPago_calls_DELETE_on_correct_endpoint() = runTest {
        var capturedPath: String? = null
        var capturedMethod: HttpMethod? = null

        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            capturedMethod = request.method
            respond(
                content = """{"success":true}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorSettingsDataSource(client, baseUrl)

        ds.disconnectMercadoPago()

        assertEquals("/api/doctors/me/mercadopago", capturedPath)
        assertEquals(HttpMethod.Delete, capturedMethod)
    }

    @Test
    fun disconnectMercadoPago_returns_success_on_200() = runTest {
        val client = buildClient { _ ->
            respond(
                content = """{"success":true}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorSettingsDataSource(client, baseUrl)

        val result = ds.disconnectMercadoPago()

        assertTrue(result.isSuccess)
    }

    @Test
    fun disconnectMercadoPago_returns_failure_on_non_2xx() = runTest {
        val client = buildClient { _ ->
            respond(
                content = """{"success":false,"error":"Not found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorSettingsDataSource(client, baseUrl)

        val result = ds.disconnectMercadoPago()

        assertTrue(result.isFailure)
    }
}
