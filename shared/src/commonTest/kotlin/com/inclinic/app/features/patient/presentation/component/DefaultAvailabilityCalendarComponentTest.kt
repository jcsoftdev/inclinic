@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.availability.application.GetAvailabilityUseCase
import com.inclinic.app.features.patient.availability.application.GetMonthAvailabilityUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val testSlot = AvailabilitySlot(id = "slot-1", startTime = "09:00", endTime = "09:30", isAvailable = true)
private val unavailableSlot = AvailabilitySlot(id = "slot-2", startTime = "10:00", endTime = "10:30", isAvailable = false)

private class FakeAvailabilityDataSource(
    private val slotsResult: Result<List<AvailabilitySlot>> = Result.success(listOf(testSlot)),
    private val monthResult: Result<Map<String, String>> = Result.success(mapOf("2026-05-22" to "open")),
) : AppointmentDataSource {
    override suspend fun getAvailability(doctorId: String, date: String) = slotsResult
    override suspend fun getMonthAvailability(doctorId: String, month: String) = monthResult
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?, homeVisitAddress: String?, homeVisitLat: Double?, homeVisitLng: Double?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String, attachments: List<String>) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class DefaultAvailabilityCalendarComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: AppointmentDataSource = FakeAvailabilityDataSource(),
        outputs: MutableList<AvailabilityCalendarComponent.Output> = mutableListOf(),
    ): DefaultAvailabilityCalendarComponent {
        return DefaultAvailabilityCalendarComponent(
            componentContext = ctx,
            doctorId = "doc-1",
            consultType = "office",
            getAvailability = GetAvailabilityUseCase(dataSource, dispatchers),
            getMonthAvailability = GetMonthAvailabilityUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun initial_state_has_correct_doctorId() = runTest {
        val component = createComponent()

        assertEquals("doc-1", component.state.value.doctorId)
    }

    @Test
    fun load_success_sets_slots_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.slots.size)
        assertEquals("slot-1", state.slots.first().id)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeAvailabilityDataSource(slotsResult = Result.failure(Exception("Network error")))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun onSlotSelected_available_slot_updates_selectedSlot() = runTest {
        val component = createComponent()

        component.onSlotSelected(testSlot)

        assertEquals(testSlot, component.state.value.selectedSlot)
    }

    @Test
    fun onSlotSelected_unavailable_slot_does_not_update_state() = runTest {
        val component = createComponent()

        component.onSlotSelected(unavailableSlot)

        assertNull(component.state.value.selectedSlot)
    }

    @Test
    fun onContinue_with_slot_and_date_emits_NavigateToBooking() = runTest {
        val outputs = mutableListOf<AvailabilityCalendarComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onSlotSelected(testSlot)

        component.onContinue()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is AvailabilityCalendarComponent.Output.NavigateToBooking)
        assertEquals("doc-1", (output as AvailabilityCalendarComponent.Output.NavigateToBooking).doctorId)
        assertEquals("slot-1", output.slotId)
    }

    @Test
    fun onContinue_emits_NavigateToBooking_with_slot_startTime() = runTest {
        val outputs = mutableListOf<AvailabilityCalendarComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onSlotSelected(testSlot)

        component.onContinue()

        assertEquals(1, outputs.size)
        val output = outputs.first() as AvailabilityCalendarComponent.Output.NavigateToBooking
        assertEquals("09:00", output.startTime)
    }

    @Test
    fun onContinue_without_slot_does_not_emit() = runTest {
        val outputs = mutableListOf<AvailabilityCalendarComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onContinue()

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onNextMonth_updates_displayMonth_forward() = runTest {
        val component = createComponent()
        val initial = component.state.value.displayMonth

        component.onNextMonth()

        val updated = component.state.value.displayMonth
        assertTrue(updated > initial)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<AvailabilityCalendarComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is AvailabilityCalendarComponent.Output.Back)
    }

    @Test
    fun onDateSelected_future_date_updates_selectedDate_and_loads_slots() = runTest {
        val component = createComponent()
        val futureDate = LocalDate(2027, 1, 15)

        component.onDateSelected(futureDate)

        assertEquals(futureDate, component.state.value.selectedDate)
        assertNull(component.state.value.selectedSlot)
    }

    @Test
    fun dayLevels_parsed_from_monthAvailability() = runTest {
        val ds = FakeAvailabilityDataSource(monthResult = Result.success(mapOf("2026-05-22" to "open", "2026-05-23" to "few")))
        val component = createComponent(dataSource = ds)

        val levels = component.state.value.dayLevels
        assertNotNull(levels[LocalDate(2026, 5, 22)])
        assertEquals(DayLevel.OPEN, levels[LocalDate(2026, 5, 22)])
        assertEquals(DayLevel.FEW, levels[LocalDate(2026, 5, 23)])
    }
}
