package com.inclinic.app.features.doctor.schedule.infrastructure

import com.inclinic.app.features.doctor.infrastructure.remote.DaySchedule
import com.inclinic.app.features.doctor.infrastructure.remote.KtorDoctorScheduleDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.WeeklySchedule
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

class KtorDoctorScheduleDataSourceTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())

    private fun buildClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient = HttpClient(MockEngine { handler(it) }) {
        install(ContentNegotiation) { json(json) }
    }

    @Test
    fun get_weekly_schedule_parses_schedules_from_doctor_profile() = runTest {
        val client = buildClient { request ->
            assertEquals(HttpMethod.Get, request.method)
            assertTrue(request.url.encodedPath.endsWith("/api/doctors/doc1"))
            respond(
                content = """{"data":{"id":"doc1","schedules":[
                    {"dayOfWeek":"MONDAY","startTime":"08:00","endTime":"13:00","maxPatients":10,"slotDuration":30,"price":120.0,"allowVisitTypeNegotiation":true,"isActive":true}
                ]}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorScheduleDataSource(client, "https://api.test")
        val result = ds.getWeeklySchedule("doc1")
        assertTrue(result.isSuccess)
        val days = result.getOrThrow().days
        assertEquals(1, days.size)
        assertEquals("MONDAY", days[0].dayOfWeek)
        assertEquals("08:00", days[0].startTime)
        assertEquals(10, days[0].maxPatients)
        assertEquals(30, days[0].slotDuration)
        assertEquals(120.0, days[0].price)
        assertTrue(days[0].allowVisitTypeNegotiation)
    }

    @Test
    fun get_weekly_schedule_is_empty_when_no_schedules() = runTest {
        val client = buildClient {
            respond(
                content = """{"data":{"id":"doc1"}}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorScheduleDataSource(client, "https://api.test")
        val result = ds.getWeeklySchedule("doc1")
        assertTrue(result.isSuccess)
        assertEquals(emptyList(), result.getOrThrow().days)
    }

    @Test
    fun save_weekly_schedule_puts_to_schedules_endpoint_and_parses_response() = runTest {
        val client = buildClient { request ->
            assertEquals(HttpMethod.Put, request.method)
            assertTrue(request.url.encodedPath.endsWith("/api/doctors/doc1/schedules"))
            respond(
                content = """{"data":[
                    {"dayOfWeek":"MONDAY","startTime":"08:00","endTime":"13:00","maxPatients":8,"isActive":true}
                ]}""",
                status = HttpStatusCode.OK,
                headers = jsonHeaders,
            )
        }
        val ds = KtorDoctorScheduleDataSource(client, "https://api.test")
        val schedule = WeeklySchedule(
            days = listOf(DaySchedule(dayOfWeek = "MONDAY", startTime = "08:00", endTime = "13:00")),
        )
        val result = ds.saveWeeklySchedule("doc1", schedule)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().days.size)
        assertEquals("MONDAY", result.getOrThrow().days[0].dayOfWeek)
    }
}
