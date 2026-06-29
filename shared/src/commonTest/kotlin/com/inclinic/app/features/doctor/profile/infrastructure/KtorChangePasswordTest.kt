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
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorChangePasswordTest {

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
    fun changePassword_calls_PATCH_on_correct_endpoint() = runTest {
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
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        ds.changePassword("old123", "new456")

        assertEquals("/api/users/me/password", capturedPath)
        assertEquals(HttpMethod.Patch, capturedMethod)
    }

    @Test
    fun changePassword_returns_success_on_200() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.changePassword("current123", "newpass123")

        assertTrue(result.isSuccess)
    }

    @Test
    fun changePassword_returns_failure_with_INVALID_CREDENTIALS_on_400() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":false,"code":"INVALID_CREDENTIALS","error":"Wrong password"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.changePassword("wrongpass", "newpass123")

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.message?.contains("INVALID_CREDENTIALS") == true,
            "Expected INVALID_CREDENTIALS in error message, got: ${result.exceptionOrNull()?.message}"
        )
    }

    @Test
    fun changePassword_returns_failure_on_generic_server_error() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":false,"error":"Internal server error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorProfileExtendedDataSource(client, baseUrl)

        val result = ds.changePassword("current123", "newpass123")

        assertTrue(result.isFailure)
    }
}
