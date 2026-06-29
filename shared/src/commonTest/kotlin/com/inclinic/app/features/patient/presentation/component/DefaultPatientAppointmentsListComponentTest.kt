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
import com.inclinic.app.features.patient.appointments.application.GetPatientAppointmentsUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

private fun listTestAppointment(
    id: String = "apt-1",
    visitType: VisitType = VisitType.VIRTUAL,
    doctorId: String = "doc-1",
): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = id, doctorId = doctorId, patientId = "pat-1", specialtyId = "sp-1",
        visitType = visitType, status = AppointmentStatus.CONFIRMED,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now + 72.hours, endsAt = now + 73.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

private class FakeListAppointmentDataSource(
    private val appointments: List<Appointment> = listOf(listTestAppointment()),
    private val error: Throwable? = null,
) : AppointmentDataSource {
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int): Result<List<Appointment>> =
        if (error != null) Result.failure(error) else Result.success(appointments)

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        appointments.find { it.id == appointmentId }
            ?.let { Result.success(it) }
            ?: Result.failure(Exception("Not found"))

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class DefaultPatientAppointmentsListComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: AppointmentDataSource = FakeListAppointmentDataSource(),
        outputs: MutableList<PatientAppointmentsListComponent.Output> = mutableListOf(),
    ): DefaultPatientAppointmentsListComponent {
        return DefaultPatientAppointmentsListComponent(
            componentContext = ctx,
            patientId = "pat-1",
            getAppointments = GetPatientAppointmentsUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_populates_appointments() = runTest {
        val ds = FakeListAppointmentDataSource(
            appointments = listOf(listTestAppointment("a-1"), listTestAppointment("a-2")),
        )
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(2, state.appointments.size)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error() = runTest {
        val component = createComponent(
            dataSource = FakeListAppointmentDataSource(error = Exception("Server error")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun onTabChange_updates_selected_tab() = runTest {
        val component = createComponent()

        component.onTabChange(AppointmentsTab.COMPLETED)

        assertEquals(AppointmentsTab.COMPLETED, component.state.value.selectedTab)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val component = createComponent(
            dataSource = FakeListAppointmentDataSource(error = Exception("Fail")),
        )
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }

    @Test
    fun onAppointmentTapped_emits_NavigateToAppointmentDetail() = runTest {
        val outputs = mutableListOf<PatientAppointmentsListComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onAppointmentTapped("apt-1")

        assertEquals(1, outputs.size)
        val output = outputs.first() as PatientAppointmentsListComponent.Output.NavigateToAppointmentDetail
        assertEquals("apt-1", output.appointmentId)
    }

    @Test
    fun onPayNow_emits_NavigateToPayment() = runTest {
        val outputs = mutableListOf<PatientAppointmentsListComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onPayNow("apt-1")

        assertEquals(1, outputs.size)
        val output = outputs.first() as PatientAppointmentsListComponent.Output.NavigateToPayment
        assertEquals("apt-1", output.appointmentId)
    }

    @Test
    fun onCancel_emits_NavigateToCancel() = runTest {
        val outputs = mutableListOf<PatientAppointmentsListComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onCancel("apt-1")

        assertEquals(1, outputs.size)
        val output = outputs.first() as PatientAppointmentsListComponent.Output.NavigateToCancel
        assertEquals("apt-1", output.appointmentId)
    }

    @Test
    fun onReschedule_virtual_maps_to_telemedicine() = runTest {
        val outputs = mutableListOf<PatientAppointmentsListComponent.Output>()
        val ds = FakeListAppointmentDataSource(
            appointments = listOf(listTestAppointment("apt-1", VisitType.VIRTUAL, "doc-1")),
        )
        val component = createComponent(dataSource = ds, outputs = outputs)

        component.onReschedule("apt-1")

        assertEquals(1, outputs.size)
        val output = outputs.first() as PatientAppointmentsListComponent.Output.NavigateToReschedule
        assertEquals("apt-1", output.appointmentId)
        assertEquals("doc-1", output.doctorId)
        assertEquals("telemedicine", output.consultType)
    }

    @Test
    fun onReschedule_home_maps_to_home() = runTest {
        val outputs = mutableListOf<PatientAppointmentsListComponent.Output>()
        val ds = FakeListAppointmentDataSource(
            appointments = listOf(listTestAppointment("apt-1", VisitType.HOME, "doc-1")),
        )
        val component = createComponent(dataSource = ds, outputs = outputs)

        component.onReschedule("apt-1")

        val output = outputs.first() as PatientAppointmentsListComponent.Output.NavigateToReschedule
        assertEquals("home", output.consultType)
    }

    @Test
    fun onReschedule_clinic_maps_to_office() = runTest {
        val outputs = mutableListOf<PatientAppointmentsListComponent.Output>()
        val ds = FakeListAppointmentDataSource(
            appointments = listOf(listTestAppointment("apt-1", VisitType.CLINIC, "doc-1")),
        )
        val component = createComponent(dataSource = ds, outputs = outputs)

        component.onReschedule("apt-1")

        val output = outputs.first() as PatientAppointmentsListComponent.Output.NavigateToReschedule
        assertEquals("office", output.consultType)
    }

    @Test
    fun onRespondReschedule_emits_NavigateToRescheduleResponse() = runTest {
        val outputs = mutableListOf<PatientAppointmentsListComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onRespondReschedule("apt-1")

        assertEquals(1, outputs.size)
        val output = outputs.first() as PatientAppointmentsListComponent.Output.NavigateToRescheduleResponse
        assertEquals("apt-1", output.appointmentId)
    }
}
