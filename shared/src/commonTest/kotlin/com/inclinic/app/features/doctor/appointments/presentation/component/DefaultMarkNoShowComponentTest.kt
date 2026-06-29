@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.appointments.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.appointments.application.GetDoctorAppointmentDetailUseCase
import com.inclinic.app.features.doctor.appointments.application.NoShowUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun markNoShowAppointment(
    startsAt: kotlin.time.Instant = Clock.System.now(),
    status: AppointmentStatus = AppointmentStatus.CONFIRMED,
): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = status,
        consultationFee = 100.0, commissionAmount = 15.0,
        startsAt = startsAt, endsAt = startsAt + 1.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now - 2.hours,
    )
}

private class FakeMarkNoShowDataSource(
    private val appointment: Appointment? = markNoShowAppointment(),
    private val loadError: Throwable? = null,
    var noShowResult: Result<Appointment> = Result.success(markNoShowAppointment(status = AppointmentStatus.NO_SHOW)),
) : DoctorAppointmentDataSource {
    var noShowCallCount = 0

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (loadError != null) Result.failure(loadError) else Result.success(appointment!!)

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
    override suspend fun confirmAppointment(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())
    override suspend fun completeAppointment(appointmentId: String, photoUrls: List<String>): Result<Appointment> =
        Result.failure(UnsupportedOperationException())
}

class DefaultMarkNoShowComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeMarkNoShowDataSource = FakeMarkNoShowDataSource(),
        outputs: MutableList<MarkNoShowComponent.Output> = mutableListOf(),
    ): DefaultMarkNoShowComponent =
        DefaultMarkNoShowComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getAppointmentDetail = GetDoctorAppointmentDetailUseCase(dataSource, dispatchers),
            noShowUseCase = NoShowUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )

    @Test
    fun load_success_sets_appointment_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.appointment)
        assertEquals("apt-1", state.appointment?.id)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeMarkNoShowDataSource(appointment = null, loadError = Exception("Network error"))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.appointment)
        assertNotNull(state.error)
    }

    @Test
    fun onReasonChanged_updates_reason_and_truncates_at_200_chars() = runTest {
        val component = createComponent()
        val longText = "x".repeat(300)

        component.onReasonChanged(longText)

        assertEquals(200, component.state.value.reason.length)
    }

    @Test
    fun canConfirm_is_false_when_reason_shorter_than_10_chars() = runTest {
        val component = createComponent()

        component.onReasonChanged("short")

        assertFalse(component.state.value.canConfirm)
    }

    @Test
    fun canConfirm_is_true_when_reason_at_least_10_chars() = runTest {
        val component = createComponent()

        component.onReasonChanged("Paciente no llegó a la cita")

        assertTrue(component.state.value.canConfirm)
    }

    @Test
    fun onConfirm_without_enough_reason_does_nothing() = runTest {
        val ds = FakeMarkNoShowDataSource()
        val outputs = mutableListOf<MarkNoShowComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onReasonChanged("short")

        component.onConfirm()

        assertEquals(0, ds.noShowCallCount)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onConfirm_success_emits_Success_output() = runTest {
        val ds = FakeMarkNoShowDataSource()
        val outputs = mutableListOf<MarkNoShowComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onReasonChanged("Paciente no se presentó")

        component.onConfirm()

        assertEquals(1, ds.noShowCallCount)
        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is MarkNoShowComponent.Output.Success)
    }

    @Test
    fun onConfirm_failure_sets_error_and_clears_isSubmitting() = runTest {
        val ds = FakeMarkNoShowDataSource()
        ds.noShowResult = Result.failure(Exception("Server error"))
        val outputs = mutableListOf<MarkNoShowComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onReasonChanged("Paciente no se presentó")

        component.onConfirm()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isSubmitting)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onConfirm_out_of_window_sets_error_without_emitting_output() = runTest {
        val ds = FakeMarkNoShowDataSource(appointment = markNoShowAppointment(startsAt = Clock.System.now() + 2.hours))
        val outputs = mutableListOf<MarkNoShowComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onReasonChanged("Paciente no se presentó")

        component.onConfirm()

        assertEquals(0, ds.noShowCallCount)
        assertTrue(outputs.isEmpty())
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<MarkNoShowComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is MarkNoShowComponent.Output.Back)
    }
}
