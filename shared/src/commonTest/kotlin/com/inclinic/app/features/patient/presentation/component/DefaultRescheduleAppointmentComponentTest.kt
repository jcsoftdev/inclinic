@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.appointments.application.RescheduleAppointmentUseCase
import com.inclinic.app.features.patient.availability.application.GetAvailabilityUseCase
import com.inclinic.app.features.patient.availability.application.GetMonthAvailabilityUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testAppointment(): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = AppointmentStatus.CONFIRMED,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now + 72.hours, endsAt = now + 73.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

private val testSlot = AvailabilitySlot(id = "slot-2", startTime = "10:00", endTime = "10:30", isAvailable = true)

private class FakeRescheduleDataSource(
    private val appointment: Appointment? = testAppointment(),
    private val rescheduleResult: Result<Appointment> = Result.success(testAppointment()),
    private val slotsResult: Result<List<AvailabilitySlot>> = Result.success(listOf(testSlot)),
    private val monthResult: Result<Map<String, String>> = Result.success(mapOf("2026-06-01" to "open")),
) : AppointmentDataSource {
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (appointment != null) Result.success(appointment) else Result.failure(Exception("Not found"))
    override suspend fun getAvailability(doctorId: String, date: String) = slotsResult
    override suspend fun getMonthAvailability(doctorId: String, month: String) = monthResult
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = rescheduleResult
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class DefaultRescheduleAppointmentComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: AppointmentDataSource = FakeRescheduleDataSource(),
        outputs: MutableList<RescheduleAppointmentComponent.Output> = mutableListOf(),
    ): DefaultRescheduleAppointmentComponent {
        return DefaultRescheduleAppointmentComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getAppointmentDetail = GetAppointmentDetailUseCase(dataSource, dispatchers),
            rescheduleAppointment = RescheduleAppointmentUseCase(dataSource, dispatchers),
            getAvailability = GetAvailabilityUseCase(dataSource, dispatchers),
            getMonthAvailability = GetMonthAvailabilityUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

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
    fun load_failure_sets_error() = runTest {
        val ds = FakeRescheduleDataSource(appointment = null)
        val component = createComponent(dataSource = ds)

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onSlotSelected_updates_selectedSlot() = runTest {
        val component = createComponent()

        component.onSlotSelected(testSlot)

        assertEquals(testSlot, component.state.value.selectedSlot)
    }

    @Test
    fun onDateSelected_updates_selectedDate_and_clears_slot() = runTest {
        val component = createComponent()
        component.onSlotSelected(testSlot)
        val newDate = LocalDate(2026, 7, 15)

        component.onDateSelected(newDate)

        assertEquals(newDate, component.state.value.selectedDate)
        assertNull(component.state.value.selectedSlot)
    }

    @Test
    fun onConfirmReschedule_without_slot_does_not_emit() = runTest {
        val outputs = mutableListOf<RescheduleAppointmentComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onConfirmReschedule()

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onConfirmReschedule_success_emits_Rescheduled() = runTest {
        val outputs = mutableListOf<RescheduleAppointmentComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onDateSelected(LocalDate(2026, 7, 15))
        component.onSlotSelected(testSlot)

        component.onConfirmReschedule()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is RescheduleAppointmentComponent.Output.Rescheduled)
    }

    @Test
    fun onConfirmReschedule_failure_sets_error() = runTest {
        val ds = FakeRescheduleDataSource(rescheduleResult = Result.failure(Exception("Already rescheduled")))
        val outputs = mutableListOf<RescheduleAppointmentComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onDateSelected(LocalDate(2026, 7, 15))
        component.onSlotSelected(testSlot)

        component.onConfirmReschedule()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isRescheduling)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onNextMonth_advances_displayMonth() = runTest {
        val component = createComponent()
        val initial = component.state.value.displayMonth

        component.onNextMonth()

        assertTrue(component.state.value.displayMonth > initial)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<RescheduleAppointmentComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is RescheduleAppointmentComponent.Output.Back)
    }
}
