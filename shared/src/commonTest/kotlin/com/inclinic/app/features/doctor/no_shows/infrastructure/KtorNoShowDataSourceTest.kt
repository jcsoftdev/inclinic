package com.inclinic.app.features.doctor.no_shows.infrastructure

import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorAppointmentDataSource
import com.inclinic.app.features.doctor.no_shows.core.model.PaymentHoldStatus
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

class KtorNoShowDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = HttpClient(MockEngine { handler(it) }) {
        install(ContentNegotiation) { json(json) }
    }

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    @Test
    fun returns_parsed_no_show_items_with_patient_name() = runTest {
        val client = buildClient {
            respond(
                content = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": "apt-1",
                      "startTime": "2026-06-29T10:00:00.000Z",
                      "price": 150.0,
                      "reason": "El paciente no se presentó.",
                      "paymentHoldStatus": "HELD",
                      "visitType": "CLINIC",
                      "patient": { "user": { "firstName": "Juan", "lastName": "García" } },
                      "specialty": { "name": "Cardiología" }
                    }
                  ]
                }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorAppointmentDataSource(client, "https://api.test")
        val result = ds.getNoShowAppointments()

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(1, items.size)
        val item = items[0]
        assertEquals("apt-1", item.id)
        assertEquals("Juan García", item.patientName)
        assertEquals(PaymentHoldStatus.HELD, item.paymentHoldStatus)
        assertEquals(150.0, item.price)
        assertEquals("Cardiología", item.specialtyName)
    }

    @Test
    fun maps_released_paymentHoldStatus_correctly() = runTest {
        val client = buildClient {
            respond(
                content = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": "apt-2",
                      "startTime": "2026-06-29T11:00:00.000Z",
                      "price": 200.0,
                      "paymentHoldStatus": "RELEASED",
                      "visitType": "VIRTUAL",
                      "patient": { "user": { "firstName": "Ana", "lastName": "Quispe" } },
                      "specialty": { "name": "Psicología" }
                    }
                  ]
                }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorAppointmentDataSource(client, "https://api.test")
        val result = ds.getNoShowAppointments()

        assertTrue(result.isSuccess)
        assertEquals(PaymentHoldStatus.RELEASED, result.getOrThrow()[0].paymentHoldStatus)
    }

    @Test
    fun maps_refunded_paymentHoldStatus_correctly() = runTest {
        val client = buildClient {
            respond(
                content = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": "apt-3",
                      "startTime": "2026-06-28T09:00:00.000Z",
                      "price": 100.0,
                      "paymentHoldStatus": "REFUNDED",
                      "visitType": "HOME",
                      "patient": { "user": { "firstName": "Luis", "lastName": "Torres" } },
                      "specialty": { "name": "Neurología" }
                    }
                  ]
                }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorAppointmentDataSource(client, "https://api.test")
        val result = ds.getNoShowAppointments()

        assertTrue(result.isSuccess)
        assertEquals(PaymentHoldStatus.REFUNDED, result.getOrThrow()[0].paymentHoldStatus)
    }

    @Test
    fun unknown_paymentHoldStatus_maps_to_unknown() = runTest {
        val client = buildClient {
            respond(
                content = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": "apt-4",
                      "startTime": "2026-06-28T09:00:00.000Z",
                      "price": 80.0,
                      "paymentHoldStatus": "SOME_FUTURE_STATUS",
                      "visitType": "CLINIC",
                      "patient": { "user": { "firstName": "Rosa", "lastName": "Mendoza" } },
                      "specialty": { "name": "Dermatología" }
                    }
                  ]
                }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorAppointmentDataSource(client, "https://api.test")
        val result = ds.getNoShowAppointments()

        assertTrue(result.isSuccess)
        assertEquals(PaymentHoldStatus.UNKNOWN, result.getOrThrow()[0].paymentHoldStatus)
    }

    @Test
    fun returns_empty_list_when_data_is_empty_array() = runTest {
        val client = buildClient {
            respond(
                content = """{"success": true, "data": []}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorAppointmentDataSource(client, "https://api.test")
        val result = ds.getNoShowAppointments()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun network_error_returns_failure() = runTest {
        val client = HttpClient(MockEngine { throw kotlinx.io.IOException("timeout") }) {
            install(ContentNegotiation) { json(json) }
        }
        val ds = KtorDoctorAppointmentDataSource(client, "https://api.test")
        val result = ds.getNoShowAppointments()
        assertTrue(result.isFailure)
    }

    @Test
    fun passes_from_and_to_query_params() = runTest {
        var capturedUrl = ""
        val client = buildClient { request ->
            capturedUrl = request.url.toString()
            respond(
                content = """{"success": true, "data": []}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorAppointmentDataSource(client, "https://api.test")
        ds.getNoShowAppointments(from = "2026-01-01", to = "2026-06-30")

        assertTrue(capturedUrl.contains("from=2026-01-01"), "URL should contain from param")
        assertTrue(capturedUrl.contains("to=2026-06-30"), "URL should contain to param")
    }

    @Test
    fun reason_falls_back_to_cancelReason_when_reason_is_null() = runTest {
        val client = buildClient {
            respond(
                content = """
                {
                  "success": true,
                  "data": [
                    {
                      "id": "apt-5",
                      "startTime": "2026-06-29T10:00:00.000Z",
                      "price": 120.0,
                      "reason": null,
                      "cancelReason": "Paciente canceló antes.",
                      "paymentHoldStatus": "HELD",
                      "visitType": "CLINIC",
                      "patient": { "user": { "firstName": "Pedro", "lastName": "López" } },
                      "specialty": { "name": "Urología" }
                    }
                  ]
                }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorAppointmentDataSource(client, "https://api.test")
        val result = ds.getNoShowAppointments()

        assertTrue(result.isSuccess)
        assertEquals("Paciente canceló antes.", result.getOrThrow()[0].reason)
    }
}
