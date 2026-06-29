package com.inclinic.app.features.doctor.patients.infrastructure

import com.inclinic.app.features.doctor.patients.infrastructure.remote.KtorDoctorPatientsDataSource
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

class KtorDoctorPatientsDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        val engine = MockEngine { request -> handler(request) }
        return HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
    }

    // ── getPatients ───────────────────────────────────────────────────────────

    @Test
    fun getPatients_hits_real_doctor_patients_url_and_parses_list_with_status() = runTest {
        var capturedPath: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            respond(
                content = """{"success":true,"data":{"patients":[{"id":"p1","name":"Ana Garcia","totalAppointments":3,"status":"active"}],"stats":{"total":1,"active":1,"premium":0}}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorPatientsDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.getPatients()

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(1, data.patients.size)
        assertEquals("p1", data.patients[0].id)
        assertEquals("Ana Garcia", data.patients[0].name)
        assertEquals(3, data.patients[0].totalAppointments)
        assertEquals("active", data.patients[0].status)
        assertEquals(1, data.stats.total)
        assertEquals(1, data.stats.active)
        assertEquals(0, data.stats.premium)
        assertEquals("/api/doctors/me/patients", capturedPath)
    }

    @Test
    fun getPatients_returns_empty_when_data_is_null() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":null}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorPatientsDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.getPatients()

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow().patients)
    }

    // ── searchPatientByEmail ──────────────────────────────────────────────────

    @Test
    fun searchPatientByEmail_hits_real_search_url_and_maps_single_patient() = runTest {
        var capturedPath: String? = null
        var capturedQuery: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            capturedQuery = request.url.parameters["email"]
            respond(
                content = """{"success":true,"data":{"id":"p2","avatar":"https://a.com/x.png","user":{"firstName":"Carlos","lastName":"Lopez","email":"carlos@test.com"}}}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorPatientsDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.searchPatientByEmail("carlos@test.com")

        assertTrue(result.isSuccess)
        val list = result.getOrThrow()
        assertEquals(1, list.size)
        assertEquals("p2", list[0].id)
        assertEquals("Carlos Lopez", list[0].name)
        assertEquals("https://a.com/x.png", list[0].avatarUrl)
        assertEquals("/api/patients/search", capturedPath)
        assertEquals("carlos@test.com", capturedQuery)
    }

    @Test
    fun searchPatientByEmail_returns_empty_list_when_not_found() = runTest {
        val client = buildClient {
            respond(
                content = """{"error":"Paciente no encontrado"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorPatientsDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.searchPatientByEmail("unknown@test.com")

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }
}
