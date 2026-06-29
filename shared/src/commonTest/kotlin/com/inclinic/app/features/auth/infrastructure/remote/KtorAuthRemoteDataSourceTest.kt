package com.inclinic.app.features.auth.infrastructure.remote

import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for [KtorAuthRemoteDataSource] using Ktor [MockEngine].
 * Each test asserts HTTP status code + optional body code → AuthError mapping,
 * and the 200 happy path verifies correct DTO deserialization.
 */
class KtorAuthRemoteDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(handler: suspend MockRequestHandleScope.(io.ktor.client.request.HttpRequestData) -> HttpResponseData): HttpClient {
        val engine = MockEngine { request -> handler(request) }
        return HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
    }

    // --- Happy path ---

    @Test
    fun status_200_returns_parsed_login_response_dto() = runTest {
        val client = buildClient {
            respond(
                content = """
                    {
                        "success": true,
                        "data": {
                            "user": {
                                "id": "u1",
                                "email": "doc@inclinic.com",
                                "firstName": "Carlos",
                                "lastName": "Ramirez",
                                "role": "DOCTOR"
                            },
                            "accessToken": "access-abc",
                            "refreshToken": "refresh-xyz"
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val result = dataSource.login(LoginRequestDto(email = "doc@inclinic.com", password = "pass"))

        assertNotNull(result.getOrNull())
        val dto = result.getOrThrow()
        assertEquals("u1", dto.user?.id)
        assertEquals("access-abc", dto.accessToken)
        assertEquals("refresh-xyz", dto.refreshToken)
    }

    @Test
    fun request_contains_correct_content_type_and_body() = runTest {
        var capturedRequest: io.ktor.client.request.HttpRequestData? = null

        val client = buildClient { request ->
            capturedRequest = request
            respond(
                content = """
                    {
                        "user": {"id":"u1","email":"e@t.com","firstName":"F","lastName":"L","role":"PATIENT"},
                        "accessToken": "a",
                        "refreshToken": "r"
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }

        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        dataSource.login(LoginRequestDto(email = "e@t.com", password = "p"))

        assertNotNull(capturedRequest)
        assertEquals(
            ContentType.Application.Json,
            capturedRequest.body.contentType,
        )
    }

    // --- Error cases ---

    @Test
    fun status_400_throws_invalid_credentials() = runTest {
        val client = buildClient {
            respond(
                content = """{"error":"Bad Request"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val result = dataSource.login(LoginRequestDto("u@t.com", "p"))

        assertIs<AuthError.InvalidCredentials>(result.exceptionOrNull())
    }

    @Test
    fun status_401_no_code_throws_invalid_credentials() = runTest {
        val client = buildClient {
            respond(
                content = """{"error":"Unauthorized"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val result = dataSource.login(LoginRequestDto("u@t.com", "p"))

        assertIs<AuthError.InvalidCredentials>(result.exceptionOrNull())
    }

    @Test
    fun status_401_code_inactive_throws_inactive_account() = runTest {
        val client = buildClient {
            respond(
                content = """{"error":"Unauthorized","code":"INACTIVE"}""",
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val result = dataSource.login(LoginRequestDto("u@t.com", "p"))

        assertIs<AuthError.InactiveAccount>(result.exceptionOrNull())
    }

    @Test
    fun status_403_code_account_suspended_throws_suspended_account() = runTest {
        val client = buildClient {
            respond(
                content = """{"error":"Forbidden","code":"ACCOUNT_SUSPENDED"}""",
                status = HttpStatusCode.Forbidden,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val result = dataSource.login(LoginRequestDto("u@t.com", "p"))

        assertIs<AuthError.SuspendedAccount>(result.exceptionOrNull())
    }

    @Test
    fun status_500_throws_server_error_with_correct_status_code() = runTest {
        val client = buildClient {
            respond(
                content = """{"error":"Internal Server Error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val result = dataSource.login(LoginRequestDto("u@t.com", "p"))

        val err = result.exceptionOrNull()
        assertIs<AuthError.ServerError>(err)
        assertEquals(500, err.status)
    }

    @Test
    fun malformed_json_throws_malformed_response() = runTest {
        val client = buildClient {
            respond(
                content = "not-valid-json{{{",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val result = dataSource.login(LoginRequestDto("u@t.com", "p"))

        assertIs<AuthError.MalformedResponse>(result.exceptionOrNull())
    }

    @Test
    fun io_exception_from_engine_throws_network_error() = runTest {
        val engine = MockEngine {
            throw kotlinx.io.IOException("Simulated connection failure")
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
        val dataSource = KtorAuthRemoteDataSource(client, baseUrl = "https://test.api.inclinic.com")
        val result = dataSource.login(LoginRequestDto("u@t.com", "p"))

        assertIs<AuthError.NetworkError>(result.exceptionOrNull())
    }
}
