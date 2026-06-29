@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.appointments.application.NoShowUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private class FakeNoShowDataSource : DoctorAppointmentDataSource {
    var noShowResult: Result<Appointment> = Result.success(makeAppointment(
        startsAt = Clock.System.now(), status = AppointmentStatus.NO_SHOW
    ))
    var noShowCallCount = 0

    override suspend fun markNoShow(appointmentId: String): Result<Appointment> {
        noShowCallCount++
        return noShowResult
    }

    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> =
        Result.success(DoctorDashboard(0, 0, 0.0, 0.0))

    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> =
        Result.success(emptyList())

    override suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>> =
        Result.success(emptyList())

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getNoShowAppointments(from: String?, to: String?): Result<List<NoShowItem>> =
        Result.success(emptyList())
}

private fun makeAppointment(startsAt: kotlin.time.Instant, status: AppointmentStatus = AppointmentStatus.CONFIRMED): Appointment {
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
        startsAt = startsAt,
        endsAt = startsAt + 1.hours,
        rescheduleCount = 0,
        paymentDeadline = null,
        notes = null,
        createdAt = now - 2.hours,
    )
}

class NoShowUseCaseTest {

    private val fakeDataSource = FakeNoShowDataSource()
    private val dispatchers = TestAppDispatchers()

    private val useCase = NoShowUseCase(
        dataSource = fakeDataSource,
        dispatchers = dispatchers,
    )

    @Test
    fun appointment_starting_now_is_within_window_and_succeeds() = runTest {
        val now = Clock.System.now()
        val appointment = makeAppointment(startsAt = now, status = AppointmentStatus.CONFIRMED)
        fakeDataSource.noShowResult = Result.success(appointment.copy(status = AppointmentStatus.NO_SHOW))

        val result = useCase(appointment)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.noShowCallCount)
    }

    @Test
    fun appointment_20_minutes_ago_is_within_window_and_succeeds() = runTest {
        val startsAt = Clock.System.now() - 20.minutes
        val appointment = makeAppointment(startsAt = startsAt, status = AppointmentStatus.CONFIRMED)
        fakeDataSource.noShowResult = Result.success(appointment.copy(status = AppointmentStatus.NO_SHOW))

        val result = useCase(appointment)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.noShowCallCount)
    }

    @Test
    fun appointment_in_2_hours_is_outside_window_and_returns_failure() = runTest {
        val startsAt = Clock.System.now() + 2.hours
        val appointment = makeAppointment(startsAt = startsAt, status = AppointmentStatus.CONFIRMED)

        val result = useCase(appointment)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        // No API call for out-of-window
        assertEquals(0, fakeDataSource.noShowCallCount)
    }

    @Test
    fun appointment_2_hours_ago_is_outside_window_and_returns_failure() = runTest {
        val startsAt = Clock.System.now() - 2.hours
        val appointment = makeAppointment(startsAt = startsAt, status = AppointmentStatus.CONFIRMED)

        val result = useCase(appointment)

        assertTrue(result.isFailure)
        assertEquals(0, fakeDataSource.noShowCallCount)
    }

    @Test
    fun wrong_status_returns_failure_without_api_call() = runTest {
        val appointment = makeAppointment(startsAt = Clock.System.now(), status = AppointmentStatus.COMPLETED)

        val result = useCase(appointment)

        assertTrue(result.isFailure)
        assertEquals(0, fakeDataSource.noShowCallCount)
    }

    @Test
    fun scheduled_status_within_window_succeeds() = runTest {
        // NoShowUseCase allows CONFIRMED or SCHEDULED
        val now = Clock.System.now()
        val appointment = makeAppointment(startsAt = now, status = AppointmentStatus.SCHEDULED)
        fakeDataSource.noShowResult = Result.success(appointment.copy(status = AppointmentStatus.NO_SHOW))

        val result = useCase(appointment)

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.noShowCallCount)
    }
}
