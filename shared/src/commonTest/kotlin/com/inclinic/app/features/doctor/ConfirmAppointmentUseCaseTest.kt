@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.appointments.application.ConfirmAppointmentUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

private class FakeDoctorDataSource : DoctorAppointmentDataSource {
    var confirmResult: Result<Appointment> = Result.success(stubAppointment(AppointmentStatus.CONFIRMED))
    var confirmCallCount = 0

    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> {
        confirmCallCount++
        return confirmResult
    }

    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> =
        Result.success(DoctorDashboard(todayCount = 0, pendingCount = 0, monthlyEarnings = 0.0, ratingAverage = 0.0))

    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> =
        Result.success(emptyList())

    override suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>> =
        Result.success(emptyList())

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun markNoShow(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getNoShowAppointments(from: String?, to: String?) =
        Result.success(emptyList<com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem>())
}

private fun stubAppointment(status: AppointmentStatus): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1",
        doctorId = "doc-1",
        patientId = "pat-1",
        specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL,
        status = status,
        consultationFee = 100.0,
        commissionAmount = 15.0,
        startsAt = now + 24.hours,
        endsAt = now + 25.hours,
        rescheduleCount = 0,
        paymentDeadline = null,
        notes = null,
        createdAt = now,
    )
}

class ConfirmAppointmentUseCaseTest {

    private val fakeDataSource = FakeDoctorDataSource()
    private val dispatchers = TestAppDispatchers()

    private val useCase = ConfirmAppointmentUseCase(
        dataSource = fakeDataSource,
        dispatchers = dispatchers,
    )

    @Test
    fun scheduled_appointment_calls_api_and_returns_success() = runTest {
        val appointment = stubAppointment(AppointmentStatus.SCHEDULED)
        fakeDataSource.confirmResult = Result.success(appointment.copy(status = AppointmentStatus.CONFIRMED))

        val result = useCase(appointment)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.confirmCallCount)
    }

    @Test
    fun pending_payment_appointment_also_succeeds() = runTest {
        // ConfirmAppointmentUseCase allows SCHEDULED or PENDING_PAYMENT
        val appointment = stubAppointment(AppointmentStatus.PENDING_PAYMENT)
        fakeDataSource.confirmResult = Result.success(appointment.copy(status = AppointmentStatus.CONFIRMED))

        val result = useCase(appointment)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.confirmCallCount)
    }

    @Test
    fun wrong_status_returns_failure_without_api_call() = runTest {
        val appointment = stubAppointment(AppointmentStatus.COMPLETED)

        val result = useCase(appointment)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        // Guard must short-circuit before hitting the API
        assertEquals(0, fakeDataSource.confirmCallCount)
    }

    @Test
    fun cancelled_status_returns_failure_without_api_call() = runTest {
        val appointment = stubAppointment(AppointmentStatus.CANCELLED_BY_PATIENT)

        val result = useCase(appointment)

        assertTrue(result.isFailure)
        assertEquals(0, fakeDataSource.confirmCallCount)
    }
}
