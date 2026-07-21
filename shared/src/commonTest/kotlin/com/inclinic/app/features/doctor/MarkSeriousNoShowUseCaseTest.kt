@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.platform.GpsFix
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.appointments.application.MarkSeriousNoShowUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeSeriousNoShowDataSource : DoctorAppointmentDataSource {
    var callCount = 0
    var lastPhotoUrls: List<String>? = null
    var lastCheckInLat: Double? = null

    override suspend fun markSeriousNoShow(
        appointmentId: String,
        photoUrls: List<String>,
        checkInLat: Double,
        checkInLng: Double,
        checkInAccuracyM: Double?,
        note: String?,
    ): Result<Appointment> {
        callCount++
        lastPhotoUrls = photoUrls
        lastCheckInLat = checkInLat
        return Result.success(makeAppt(AppointmentStatus.NO_SHOW, VisitType.HOME))
    }

    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>, checkInLat: Double?, checkInLng: Double?, checkInAccuracyM: Double?): Result<Appointment> =
        Result.failure(UnsupportedOperationException())
    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> = Result.success(DoctorDashboard(0, 0, 0.0, 0.0))
    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> = Result.success(emptyList())
    override suspend fun getAvailability(doctorId: String, date: String): Result<List<AvailabilitySlot>> = Result.success(emptyList())
    override suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>> = Result.success(emptyList())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun markNoShow(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getNoShowAppointments(from: String?, to: String?) = Result.success(emptyList<com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem>())
    override suspend fun getPendingClosureAppointments(from: String?, to: String?): Result<List<PendingClosureItem>> = Result.failure(UnsupportedOperationException())
}

private fun makeAppt(status: AppointmentStatus, visitType: VisitType): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = visitType, status = status, consultationFee = 100.0, commissionAmount = 10.0,
        startsAt = now - 1.hours, endsAt = now, rescheduleCount = 0, paymentDeadline = null,
        notes = null, createdAt = now - 2.hours,
    )
}

class MarkSeriousNoShowUseCaseTest {

    private val ds = FakeSeriousNoShowDataSource()
    private val useCase = MarkSeriousNoShowUseCase(ds, TestAppDispatchers())
    private val fix = GpsFix(lat = -12.05, lng = -77.04, accuracyMeters = 5.0)

    @Test
    fun virtual_visit_is_rejected_without_api_call() = runTest {
        val result = useCase(makeAppt(AppointmentStatus.CONFIRMED, VisitType.VIRTUAL), listOf("u0"), fix)
        assertTrue(result.isFailure)
        assertEquals(0, ds.callCount)
    }

    @Test
    fun no_photos_is_rejected_without_api_call() = runTest {
        val result = useCase(makeAppt(AppointmentStatus.CONFIRMED, VisitType.HOME), emptyList(), fix)
        assertTrue(result.isFailure)
        assertEquals(0, ds.callCount)
    }

    @Test
    fun wrong_status_is_rejected_without_api_call() = runTest {
        val result = useCase(makeAppt(AppointmentStatus.COMPLETED, VisitType.HOME), listOf("u0"), fix)
        assertTrue(result.isFailure)
        assertEquals(0, ds.callCount)
    }

    @Test
    fun valid_home_visit_forwards_evidence() = runTest {
        val result = useCase(makeAppt(AppointmentStatus.CONFIRMED, VisitType.HOME), listOf("u0"), fix)
        assertTrue(result.isSuccess)
        assertEquals(1, ds.callCount)
        assertEquals(listOf("u0"), ds.lastPhotoUrls)
        assertEquals(-12.05, ds.lastCheckInLat)
    }
}
