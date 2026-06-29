package com.inclinic.app.core

import com.inclinic.app.core.error.ApiError
import com.inclinic.app.core.error.ApiResult
import com.inclinic.app.core.network.runApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * Verifies HTTP status → [ApiError] mapping implemented in [runApi].
 */
class ApiErrorMappingTest {

    private fun clientWith(status: HttpStatusCode, body: String = "{}"): HttpClient =
        HttpClient(MockEngine { _ ->
            respond(
                content = body,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }) {
            install(ContentNegotiation) { json() }
            expectSuccess = true
        }

    @Test
    fun status_401_maps_to_Unauthorized() = runTest {
        val client = clientWith(HttpStatusCode.Unauthorized)
        val result = runApi { client.get("/test") }
        assertIs<ApiResult.Err>(result)
        assertIs<ApiError.Unauthorized>(result.error)
    }

    @Test
    fun status_403_maps_to_Forbidden() = runTest {
        val client = clientWith(HttpStatusCode.Forbidden)
        val result = runApi { client.get("/test") }
        assertIs<ApiResult.Err>(result)
        assertIs<ApiError.Forbidden>(result.error)
    }

    @Test
    fun status_404_maps_to_NotFound() = runTest {
        val client = clientWith(HttpStatusCode.NotFound)
        val result = runApi { client.get("/test") }
        assertIs<ApiResult.Err>(result)
        assertIs<ApiError.NotFound>(result.error)
    }

    @Test
    fun status_409_maps_to_Conflict() = runTest {
        val client = clientWith(HttpStatusCode.Conflict)
        val result = runApi { client.get("/test") }
        assertIs<ApiResult.Err>(result)
        assertIs<ApiError.Conflict>(result.error)
    }

    @Test
    fun status_500_maps_to_Server() = runTest {
        val client = clientWith(HttpStatusCode.InternalServerError)
        val result = runApi { client.get("/test") }
        assertIs<ApiResult.Err>(result)
        assertIs<ApiError.Server>(result.error)
    }

    @Test
    fun status_503_maps_to_Server() = runTest {
        val client = clientWith(HttpStatusCode.ServiceUnavailable)
        val result = runApi { client.get("/test") }
        assertIs<ApiResult.Err>(result)
        assertIs<ApiError.Server>(result.error)
    }
}
