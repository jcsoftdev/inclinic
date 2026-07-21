package com.inclinic.app.features.admin.infrastructure

import com.inclinic.app.core.error.isNotFoundError
import com.inclinic.app.features.admin.infrastructure.remote.KtorAdminDataSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * RED → GREEN regression test for review finding 1: the by-id admin lookups
 * (`getAppointmentDetail`, `getDoctorDetail`, `getPendingDoctorById`) must throw
 * something [isNotFoundError] recognizes on a real HTTP 404, so the [NotFound]
 * UI state is reachable — see [com.inclinic.app.core.util.DetailLoadState].
 *
 * Before the fix, these requests neither set `expectSuccess = true` nor went
 * through [com.inclinic.app.core.network.runApi], so a 404 response body that
 * still parsed as `ApiEnvelope<T>(data = null)` fell through to the datasource's
 * own `?: error(...)`, throwing a plain [IllegalStateException] that
 * [isNotFoundError] does not recognize.
 */
class KtorAdminDataSourceNotFoundTest {

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

    private fun notFoundResponse(): suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData = { _ ->
        respond(
            content = """{"success":false,"error":"Not found"}""",
            status = HttpStatusCode.NotFound,
            headers = headersOf(HttpHeaders.ContentType, "application/json"),
        )
    }

    @Test
    fun getAppointmentDetail_on_404_yields_a_not_found_error() = runTest {
        val ds = KtorAdminDataSource(buildClient(notFoundResponse()), baseUrl)

        val result = ds.getAppointmentDetail("missing-id")

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.isNotFoundError() == true,
            "Expected isNotFoundError() to recognize the 404 failure, got: ${result.exceptionOrNull()}",
        )
    }

    @Test
    fun getDoctorDetail_on_404_yields_a_not_found_error() = runTest {
        val ds = KtorAdminDataSource(buildClient(notFoundResponse()), baseUrl)

        val result = ds.getDoctorDetail("missing-id")

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.isNotFoundError() == true,
            "Expected isNotFoundError() to recognize the 404 failure, got: ${result.exceptionOrNull()}",
        )
    }

    @Test
    fun getPendingDoctorById_on_404_yields_a_not_found_error() = runTest {
        val ds = KtorAdminDataSource(buildClient(notFoundResponse()), baseUrl)

        val result = ds.getPendingDoctorById("missing-id")

        assertTrue(result.isFailure)
        assertTrue(
            result.exceptionOrNull()?.isNotFoundError() == true,
            "Expected isNotFoundError() to recognize the 404 failure, got: ${result.exceptionOrNull()}",
        )
    }
}
