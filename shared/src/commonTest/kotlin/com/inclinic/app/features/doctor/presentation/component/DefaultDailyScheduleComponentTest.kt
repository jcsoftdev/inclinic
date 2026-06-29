package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import com.inclinic.app.features.doctor.schedule.application.GetDailyScheduleUseCase
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

// ── Fake ─────────────────────────────────────────────────────────────────────

private class FakeDoctorAppointmentDataSource : DoctorAppointmentDataSource {
    var dailyScheduleResult: Result<List<Appointment>> = Result.success(emptyList())
    var callCount = 0

    override suspend fun getDashboard(doctorId: String): Result<DoctorDashboard> =
        Result.success(DoctorDashboard(0, 0, 0.0, 5.0))

    override suspend fun getDailySchedule(doctorId: String, date: String): Result<List<Appointment>> {
        callCount++
        return dailyScheduleResult
    }

    override suspend fun getWeeklySchedule(doctorId: String, weekStart: String): Result<List<DaySummary>> =
        Result.success(emptyList())

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun markNoShow(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())
    override suspend fun getNoShowAppointments(from: String?, to: String?) =
        Result.success(emptyList<com.inclinic.app.features.doctor.no_shows.core.model.NoShowItem>())
}

private fun makeAppointment(id: String): Appointment = Appointment(
    id = id,
    doctorId = "doc-1",
    patientId = "pat-1",
    specialtyId = "sp-1",
    visitType = VisitType.VIRTUAL,
    status = AppointmentStatus.SCHEDULED,
    consultationFee = 100.0,
    commissionAmount = 10.0,
    startsAt = Instant.fromEpochMilliseconds(0L),
    endsAt = Instant.fromEpochMilliseconds(3_600_000L),
    rescheduleCount = 0,
    paymentDeadline = null,
    notes = null,
    createdAt = Instant.fromEpochMilliseconds(0L),
)

// ── Tests ─────────────────────────────────────────────────────────────────────

class DefaultDailyScheduleComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val dispatchers = TestAppDispatchers()
    private val fakeDs = FakeDoctorAppointmentDataSource()
    private var capturedOutput: DailyScheduleComponent.Output? = null

    private fun createComponent(): DefaultDailyScheduleComponent {
        capturedOutput = null
        return DefaultDailyScheduleComponent(
            componentContext  = DefaultComponentContext(lifecycle),
            doctorId          = "doc-1",
            getDailySchedule  = GetDailyScheduleUseCase(fakeDs, dispatchers),
            dispatchers       = dispatchers,
            onOutput          = { capturedOutput = it },
        )
    }

    // ── Loading / success state ───────────────────────────────────────────────

    @Test
    fun initial_load_success_populates_appointments_and_clears_error() = runTest {
        val appts = listOf(makeAppointment("a1"), makeAppointment("a2"))
        fakeDs.dailyScheduleResult = Result.success(appts)

        val component = createComponent()

        assertFalse(component.state.value.isLoading, "isLoading must be false after load")
        assertNull(component.state.value.error, "error must be null on success")
        assertEquals(appts.size, component.state.value.appointments.size)
    }

    @Test
    fun initial_load_empty_list_results_in_empty_appointments() = runTest {
        fakeDs.dailyScheduleResult = Result.success(emptyList())

        val component = createComponent()

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertTrue(component.state.value.appointments.isEmpty())
    }

    // ── Error state ───────────────────────────────────────────────────────────

    @Test
    fun initial_load_failure_sets_error_and_clears_appointments() = runTest {
        fakeDs.dailyScheduleResult = Result.failure(RuntimeException("Network error"))

        val component = createComponent()

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.appointments.isEmpty())
    }

    // ── Day navigation ────────────────────────────────────────────────────────

    @Test
    fun onPreviousDay_triggers_reload_for_previous_date() = runTest {
        fakeDs.dailyScheduleResult = Result.success(emptyList())
        val component = createComponent()
        val dateAfterInit = component.state.value.date

        component.onPreviousDay()

        assertEquals(2, fakeDs.callCount, "getDailySchedule must be called twice: init + onPreviousDay")
        val newDate = component.state.value.date
        assertNotNull(newDate)
        assertNotNull(dateAfterInit)
        assertTrue(newDate < dateAfterInit, "date must be one day earlier")
    }

    @Test
    fun onNextDay_triggers_reload_for_next_date() = runTest {
        fakeDs.dailyScheduleResult = Result.success(emptyList())
        val component = createComponent()
        val dateAfterInit = component.state.value.date

        component.onNextDay()

        assertEquals(2, fakeDs.callCount)
        val newDate = component.state.value.date
        assertNotNull(newDate)
        assertNotNull(dateAfterInit)
        assertTrue(newDate > dateAfterInit, "date must be one day later")
    }

    // ── Navigation outputs ────────────────────────────────────────────────────

    @Test
    fun onAppointmentTap_emits_NavigateToAppointmentDetail() {
        fakeDs.dailyScheduleResult = Result.success(emptyList())
        val component = createComponent()

        component.onAppointmentTap("appt-99")

        assertEquals(
            DailyScheduleComponent.Output.NavigateToAppointmentDetail("appt-99"),
            capturedOutput,
        )
    }

    @Test
    fun onOpenRescheduleQueue_emits_OpenRescheduleQueue() {
        fakeDs.dailyScheduleResult = Result.success(emptyList())
        val component = createComponent()

        component.onOpenRescheduleQueue()

        assertEquals(DailyScheduleComponent.Output.OpenRescheduleQueue, capturedOutput)
    }

    @Test
    fun onBack_emits_Back() {
        fakeDs.dailyScheduleResult = Result.success(emptyList())
        val component = createComponent()

        component.onBack()

        assertEquals(DailyScheduleComponent.Output.Back, capturedOutput)
    }
}
