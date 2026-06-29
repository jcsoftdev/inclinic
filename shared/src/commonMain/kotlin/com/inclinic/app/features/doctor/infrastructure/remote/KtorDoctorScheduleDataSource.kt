package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

/** Subset of the doctor profile response we need to read schedules. */
@Serializable
private data class DoctorProfileScheduleEnvelope(
    val schedules: List<DaySchedule> = emptyList(),
)

@Serializable
private data class SaveSchedulesRequest(
    val schedules: List<DaySchedule>,
)

/**
 * Wires the weekly schedule to the REAL backend routes:
 *  - load: `GET /api/doctors/{id}` (schedules live on the doctor profile)
 *  - save: `PUT /api/doctors/{id}/schedules` with body `{ "schedules": [...] }`
 */
class KtorDoctorScheduleDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorScheduleDataSource {

    override suspend fun getWeeklySchedule(doctorId: String): Result<WeeklySchedule> = runCatching {
        val profile = client.get {
            url("$baseUrl/api/doctors/$doctorId")
        }.body<ApiEnvelope<DoctorProfileScheduleEnvelope>>().data
        WeeklySchedule(days = profile?.schedules ?: emptyList())
    }

    override suspend fun saveWeeklySchedule(
        doctorId: String,
        schedule: WeeklySchedule,
    ): Result<WeeklySchedule> = runCatching {
        val saved = client.put {
            url("$baseUrl/api/doctors/$doctorId/schedules")
            contentType(ContentType.Application.Json)
            setBody(SaveSchedulesRequest(schedules = schedule.days))
        }.body<ApiEnvelope<List<DaySchedule>>>().data ?: error("Schedule save failed")
        WeeklySchedule(days = saved)
    }
}
