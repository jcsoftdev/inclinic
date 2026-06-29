package com.inclinic.app.features.doctor.medical_records.infrastructure

import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorMedicalRecordDataSource
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

class KtorDoctorMedicalRecordDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        val engine = MockEngine { request -> handler(request) }
        return HttpClient(engine) {
            install(ContentNegotiation) { json(json) }
        }
    }

    @Test
    fun getMedicalRecords_hits_patient_medical_history_url_and_maps_nested_dto() = runTest {
        var capturedPath: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            respond(
                content = """
                    {"success":true,"data":[
                      {"id":"r1","appointmentId":"a1","patientId":"pat1","doctorId":"doc1",
                       "diagnosis":"Hipertensión controlada","symptoms":"Cefalea","treatmentPlan":"Dieta baja en sodio",
                       "notes":null,"isLocked":false,"createdAt":"2026-03-08T10:00:00Z",
                       "doctor":{"user":{"firstName":"Luis","lastName":"Vargas"}},
                       "specialty":{"name":"Cardiología"}}
                    ]}
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorMedicalRecordDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.getMedicalRecords("pat1")

        assertTrue(result.isSuccess)
        val records = result.getOrThrow()
        assertEquals(1, records.size)
        assertEquals("r1", records[0].id)
        assertEquals("Hipertensión controlada", records[0].diagnosis)
        assertEquals("Dr. Luis Vargas", records[0].doctorName)
        assertEquals("Cardiología", records[0].specialtyName)
        assertEquals("/api/patients/pat1/medical-history", capturedPath)
    }

    @Test
    fun createMedicalRecord_sends_treatmentPlan_and_structured_prescriptions() = runTest {
        var capturedPath: String? = null
        var capturedBody: String? = null
        val client = buildClient { request ->
            capturedPath = request.url.encodedPath
            capturedBody = (request.body as io.ktor.http.content.TextContent).text
            respond(
                content = """{"success":true,"data":{"id":"r9","appointmentId":"a1","patientId":"pat1","doctorId":"doc1","diagnosis":"Dx","symptoms":"","treatment":"","prescription":null,"notes":null,"createdAt":"2026-03-08T10:00:00Z"}}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorMedicalRecordDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.createMedicalRecord(
            com.inclinic.app.features.doctor.infrastructure.remote.CreateMedicalRecordRequest(
                appointmentId = "a1",
                patientId = "pat1",
                diagnosis = "Diabetes tipo 2",
                symptoms = "Fatiga",
                treatment = "Metformina 850mg",
                prescription = "Metformina 850mg cada 12h",
                notes = null,
            ),
        )

        assertTrue(result.isSuccess)
        assertEquals("/api/medical-records", capturedPath)
        val body = capturedBody ?: ""
        assertTrue(body.contains("\"treatmentPlan\""), "should send treatmentPlan, was: $body")
        assertTrue(body.contains("\"prescriptions\""), "should send prescriptions array, was: $body")
        assertTrue(!body.contains("\"treatment\":"), "should NOT send flat treatment field, was: $body")
    }

    @Test
    fun createMedicalRecord_omits_appointmentId_when_null() = runTest {
        var capturedBody: String? = null
        val client = buildClient { request ->
            capturedBody = (request.body as io.ktor.http.content.TextContent).text
            respond(
                content = """{"success":true,"data":{"id":"r9","appointmentId":"","patientId":"pat1","doctorId":"doc1","diagnosis":"Dx","symptoms":"","treatment":"","prescription":null,"notes":null,"createdAt":"2026-03-08T10:00:00Z"}}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorMedicalRecordDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.createMedicalRecord(
            com.inclinic.app.features.doctor.infrastructure.remote.CreateMedicalRecordRequest(
                appointmentId = null,
                patientId = "pat1",
                diagnosis = "Diabetes tipo 2",
                symptoms = "Fatiga",
                treatment = "Metformina 850mg",
                prescription = null,
                notes = null,
            ),
        )

        assertTrue(result.isSuccess)
        val body = capturedBody ?: ""
        assertTrue(!body.contains("\"appointmentId\""), "should omit appointmentId when null, was: $body")
    }

    @Test
    fun getMedicalRecords_returns_empty_when_data_null() = runTest {
        val client = buildClient {
            respond(
                content = """{"success":true,"data":null}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        val ds = KtorDoctorMedicalRecordDataSource(client, baseUrl = "https://test.api.inclinic.com")

        val result = ds.getMedicalRecords("pat1")

        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow())
    }
}
