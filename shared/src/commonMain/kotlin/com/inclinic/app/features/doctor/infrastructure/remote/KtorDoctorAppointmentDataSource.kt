package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

@Serializable
private data class DashboardDto(
    val todayCount: Int = 0,
    val pendingCount: Int = 0,
    val monthlyEarnings: Double = 0.0,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val completedCount: Int = 0,
    val completedThisMonth: Int = 0,
    val patientsCount: Int = 0,
    val recurringPatientsCount: Int = 0,
    val completedTodayPct: Int = 0,
)

@Serializable
private data class DaySummaryDto(
    val date: String,
    val appointmentCount: Int,
)

class KtorDoctorAppointmentDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorAppointmentDataSource {

    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> = runCatching {
        val dto = client.get {
            url("$baseUrl/api/doctors/$doctorId/dashboard")
        }.body<ApiEnvelope<DashboardDto>>().data ?: error("Dashboard data missing")
        DoctorDashboard(
            todayCount = dto.todayCount,
            pendingCount = dto.pendingCount,
            monthlyEarnings = dto.monthlyEarnings,
            ratingAverage = dto.ratingAverage,
            ratingCount = dto.ratingCount,
            completedCount = dto.completedCount,
            completedThisMonth = dto.completedThisMonth,
            patientsCount = dto.patientsCount,
            recurringPatientsCount = dto.recurringPatientsCount,
            completedTodayPct = dto.completedTodayPct,
        )
    }

    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> = runCatching {
        client.get {
            url("$baseUrl/api/appointments")
            parameter("doctorId", doctorId)
            parameter("date", date)
        }.body<ApiEnvelope<List<Appointment>>>().data ?: emptyList()
    }

    override suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>> = runCatching {
        val dtos = client.get {
            url("$baseUrl/api/appointments")
            parameter("doctorId", doctorId)
            parameter("week", weekStart)
        }.body<ApiEnvelope<List<DaySummaryDto>>>().data ?: emptyList()
        dtos.map { DaySummary(date = it.date, appointmentCount = it.appointmentCount) }
    }

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = runCatching {
        client.get {
            url("$baseUrl/api/appointments/$appointmentId")
        }.body<ApiEnvelope<Appointment>>().data ?: error("Appointment not found")
    }

    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> = runCatching {
        client.patch {
            url("$baseUrl/api/appointments/$appointmentId/confirm")
            contentType(ContentType.Application.Json)
            setBody(emptyMap<String, String>())
        }.body<ApiEnvelope<Appointment>>().data ?: error("Confirm failed")
    }

    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>): Result<Appointment> = runCatching {
        client.patch {
            url("$baseUrl/api/appointments/$appointmentId/complete")
            contentType(ContentType.Application.Json)
            setBody(mapOf("photoUrls" to photoUrls))
        }.body<ApiEnvelope<Appointment>>().data ?: error("Complete failed")
    }

    override suspend fun markNoShow(appointmentId: String): Result<Appointment> = runCatching {
        client.patch {
            url("$baseUrl/api/appointments/$appointmentId/no-show")
            contentType(ContentType.Application.Json)
            setBody(emptyMap<String, String>())
        }.body<ApiEnvelope<Appointment>>().data ?: error("No-show failed")
    }
}
