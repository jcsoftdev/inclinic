package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem
import com.inclinic.app.features.doctor.no_shows.core.model.PaymentHoldStatus
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
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

// ── No-Show DTOs ──────────────────────────────────────────────────────────────

@Serializable
private data class NoShowPatientUserDto(
    val firstName: String = "",
    val lastName: String = "",
)

@Serializable
private data class NoShowPatientDto(
    val user: NoShowPatientUserDto = NoShowPatientUserDto(),
)

@Serializable
private data class NoShowSpecialtyDto(
    val name: String = "",
)

@Serializable
private data class NoShowItemDto(
    val id: String = "",
    val startTime: String = "",
    val price: Double = 0.0,
    val reason: String? = null,
    val cancelReason: String? = null,
    val notes: String? = null,
    val paymentHoldStatus: String? = null,
    val visitType: String = "CLINIC",
    val patient: NoShowPatientDto = NoShowPatientDto(),
    val specialty: NoShowSpecialtyDto = NoShowSpecialtyDto(),
)

private fun NoShowItemDto.toDomain() = NoShowItem(
    id = id,
    patientName = "${patient.user.firstName} ${patient.user.lastName}".trim(),
    startTime = startTime,
    price = price,
    reason = reason ?: cancelReason,
    specialtyName = specialty.name,
    visitType = visitType,
    paymentHoldStatus = when (paymentHoldStatus) {
        "HELD"     -> PaymentHoldStatus.HELD
        "RELEASED" -> PaymentHoldStatus.RELEASED
        "REFUNDED" -> PaymentHoldStatus.REFUNDED
        else       -> PaymentHoldStatus.UNKNOWN
    },
)

// ── Dashboard DTO ─────────────────────────────────────────────────────────────

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

/**
 * Mirrors the nested `patient.user` / `specialty` shape from the backend's
 * GET /api/appointments?needsClosure=true payload (see appointment.service.ts).
 *
 * The Prisma `Appointment` model's `type` column is an `AppointmentType` enum
 * (CONSULTATION | FOLLOW_UP | THERAPY | TELEMEDICINE) — it does NOT carry the
 * visit modality (clinic/home/virtual). That modality is instead carried by
 * the two independent `isTelemedicine` / `isHomeVisit` booleans, same as on
 * the patient-side `AppointmentDto` (see KtorAppointmentDataSource.kt).
 */
@Serializable
private data class PendingClosureItemDto(
    val id: String = "",
    val startTime: String = "",
    val price: Double = 0.0,
    val isTelemedicine: Boolean = false,
    val isHomeVisit: Boolean = false,
    val patient: NoShowPatientDto = NoShowPatientDto(),
    val specialty: NoShowSpecialtyDto = NoShowSpecialtyDto(),
)

private fun PendingClosureItemDto.toDomain() = PendingClosureItem(
    id = id,
    patientName = "${patient.user.firstName} ${patient.user.lastName}".trim(),
    startTime = startTime,
    price = price,
    specialtyName = specialty.name,
    visitType = when {
        isTelemedicine -> "VIRTUAL"
        isHomeVisit -> "HOME"
        else -> "CLINIC"
    },
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

    override suspend fun getNoShowAppointments(
        from: String?,
        to: String?,
    ): Result<List<NoShowItem>> = runCatching {
        val dtos = client.get {
            url("$baseUrl/api/appointments")
            parameter("status", "NO_SHOW")
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
        }.body<ApiEnvelope<List<NoShowItemDto>>>().data ?: emptyList()
        dtos.map { it.toDomain() }
    }

    override suspend fun getPendingClosureAppointments(
        from: String?,
        to: String?,
    ): Result<List<PendingClosureItem>> = runCatching {
        val dtos = client.get {
            url("$baseUrl/api/appointments")
            parameter("needsClosure", "true")
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
        }.body<ApiEnvelope<List<PendingClosureItemDto>>>().data ?: emptyList()
        dtos.map { it.toDomain() }
    }
}
