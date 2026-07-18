package com.inclinic.app.features.doctor.prescriptions.infrastructure

import com.inclinic.app.core.error.ApiError
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.KtorDoctorPrescriptionsDataSource
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.CreatePrescriptionRequestDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.UpdatePrescriptionRequestDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.UpdatePrescriptionItemDto
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
import kotlin.test.assertIs
import kotlin.test.assertTrue

class KtorDoctorPrescriptionsDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient = HttpClient(MockEngine { handler(it) }) {
        install(ContentNegotiation) { json(json) }
        expectSuccess = true
    }

    private val prescriptionJson = """
        {"success":true,"data":{
            "id":"rx-1","appointmentId":"apt-1","doctorId":"doc-1","patientId":"pat-1",
            "instructions":"Tomar con agua","doctorFullName":"Dr. Lopez","doctorSignature":"RX-SIG","createdAt":"2026-06-01T10:00:00Z",
            "items":[{"id":"item-1","medicationName":"Losartan 50 mg","dosage":"50mg","frequency":"Cada 12h","duration":"30 dias","order":0}]
        }}
    """.trimIndent()

    @Test
    fun getPrescription_hits_real_endpoint() = runTest {
        var capturedPath: String? = null
        val client = buildClient {
            capturedPath = it.url.encodedPath
            respond(content = prescriptionJson, status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val ds = KtorDoctorPrescriptionsDataSource(client, "https://api.test")
        val result = ds.getPrescription("rx-1")
        assertTrue(result.isSuccess)
        assertEquals("rx-1", result.getOrThrow().id)
        assertEquals("Losartan 50 mg", result.getOrThrow().items[0].medicationName)
        assertEquals("/api/prescriptions/rx-1", capturedPath)
    }

    @Test
    fun updatePrescription_puts_to_real_endpoint() = runTest {
        var capturedPath: String? = null
        var capturedMethod: HttpMethod? = null
        val client = buildClient {
            capturedPath = it.url.encodedPath
            capturedMethod = it.method
            respond(content = prescriptionJson, status = HttpStatusCode.OK, headers = jsonHeaders)
        }
        val ds = KtorDoctorPrescriptionsDataSource(client, "https://api.test")
        val body = UpdatePrescriptionRequestDto(
            instructions = "Tomar con agua",
            items = listOf(UpdatePrescriptionItemDto(medicationName = "Losartan 50 mg", dosage = "50mg", frequency = "Cada 12h", duration = "30 dias")),
        )
        val result = ds.updatePrescription("rx-1", body)
        assertTrue(result.isSuccess)
        assertEquals("/api/prescriptions/rx-1", capturedPath)
        assertEquals(HttpMethod.Put, capturedMethod)
    }

    @Test
    fun createPrescription_posts_to_prescriptions_and_returns_dto() = runTest {
        val prescriptionCreateJson = """
            {"success":true,"data":{
                "id":"rx-new","appointmentId":"apt-1","doctorId":"doc-1","patientId":"pat-1",
                "instructions":"","doctorFullName":"Dr. Lopez","doctorSignature":"RX-SIG","createdAt":"2026-06-01T10:00:00Z",
                "items":[{"id":"item-1","medicationName":"Amoxicilina 500mg"}]
            }}
        """.trimIndent()
        val client = buildClient {
            assertEquals("/api/prescriptions", it.url.encodedPath)
            assertEquals(HttpMethod.Post, it.method)
            respond(
                content = prescriptionCreateJson,
                status = HttpStatusCode.Created,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorPrescriptionsDataSource(client, "https://api.test")

        val result = ds.createPrescription(
            CreatePrescriptionRequestDto(
                appointmentId = "apt-1",
                items = listOf(UpdatePrescriptionItemDto(medicationName = "Amoxicilina 500mg")),
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("rx-new", result.getOrThrow().id)
    }

    @Test
    fun createPrescription_conflict_surfaces_backend_message() = runTest {
        val conflictJson = """
            {"error":"Esta cita ya tiene una receta. Edítala en su lugar.","code":"PRESCRIPTION_ALREADY_EXISTS"}
        """.trimIndent()
        val client = buildClient {
            respond(content = conflictJson, status = HttpStatusCode.Conflict, headers = jsonHeaders)
        }
        val ds = KtorDoctorPrescriptionsDataSource(client, "https://api.test")

        val result = ds.createPrescription(
            CreatePrescriptionRequestDto(
                appointmentId = "apt-1",
                items = listOf(UpdatePrescriptionItemDto(medicationName = "Amoxicilina 500mg")),
            )
        )

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertIs<ApiError.Conflict>(error)
        assertEquals("Esta cita ya tiene una receta. Edítala en su lugar.", error.message)
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorDoctorPrescriptionsDataSource(client, "https://api.test")
        val result = ds.getPrescription("rx-1")
        assertTrue(result.isFailure)
    }
}
