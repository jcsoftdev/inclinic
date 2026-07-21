package com.inclinic.app.features.doctor.no_shows.fakes

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem
import com.inclinic.app.features.doctor.no_shows.core.model.PaymentHoldStatus
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
import kotlin.time.Clock

/** Minimal test double for [DoctorAppointmentDataSource].
 *  Only [getNoShowAppointments] is exercised; all others short-circuit. */
class FakeNoShowAppointmentDataSource : DoctorAppointmentDataSource {

    var noShowResult: Result<List<NoShowItem>> = Result.success(emptyList())
    var lastFrom: String? = null
    var lastTo: String? = null
    var callCount: Int = 0

    override suspend fun getNoShowAppointments(from: String?, to: String?): Result<List<NoShowItem>> {
        lastFrom = from
        lastTo = to
        callCount++
        return noShowResult
    }

    // ── Unsupported stubs ────────────────────────────────────────────────────

    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> =
        Result.success(DoctorDashboard(0, 0, 0.0, 0.0))

    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> =
        Result.success(emptyList())

    override suspend fun getAvailability(doctorId: String, date: String): Result<List<com.inclinic.app.core.model.AvailabilitySlot>> =
        Result.success(emptyList())

    override suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>> =
        Result.success(emptyList())

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>, checkInLat: Double?, checkInLng: Double?, checkInAccuracyM: Double?): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun markNoShow(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getPendingClosureAppointments(from: String?, to: String?): Result<List<PendingClosureItem>> =
        Result.failure(UnsupportedOperationException())
}

// ── Stub helpers ──────────────────────────────────────────────────────────────

fun stubNoShowItem(
    id: String,
    paymentHoldStatus: PaymentHoldStatus = PaymentHoldStatus.HELD,
) = NoShowItem(
    id = id,
    patientName = "María García",
    startTime = "2026-06-29T10:00:00.000Z",
    price = 150.0,
    reason = "El paciente no se presentó.",
    specialtyName = "Cardiología",
    visitType = "CLINIC",
    paymentHoldStatus = paymentHoldStatus,
)
