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
import com.inclinic.app.features.patient.appointments.application.RequestVisitTypeChangeUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
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
        startsAt = now + 48.hours, endsAt = now + 49.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

private class FakeChangeVisitTypeDataSource(
    private val appointment: Appointment? = testAppointment(),
    private val changeResult: Result<Unit> = Result.success(Unit),
) : AppointmentDataSource {
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (appointment != null) Result.success(appointment) else Result.failure(Exception("Not found"))
    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?, homeVisitAddress: String?, homeVisitLat: Double?, homeVisitLng: Double?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String, attachments: List<String>) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = changeResult
}

class DefaultChangeVisitTypeComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: AppointmentDataSource = FakeChangeVisitTypeDataSource(),
        outputs: MutableList<ChangeVisitTypeComponent.Output> = mutableListOf(),
    ): DefaultChangeVisitTypeComponent {
        return DefaultChangeVisitTypeComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getAppointmentDetail = GetAppointmentDetailUseCase(dataSource, dispatchers),
            requestVisitTypeChange = RequestVisitTypeChangeUseCase(dataSource, dispatchers),
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
        val ds = FakeChangeVisitTypeDataSource(appointment = null)
        val component = createComponent(dataSource = ds)

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onNewVisitTypeSelected_updates_newVisitType() = runTest {
        val component = createComponent()

        component.onNewVisitTypeSelected(VisitType.HOME)

        assertEquals(VisitType.HOME, component.state.value.newVisitType)
    }

    @Test
    fun onAddressChanged_updates_address() = runTest {
        val component = createComponent()

        component.onAddressChanged("Av. Javier Prado 123")

        assertEquals("Av. Javier Prado 123", component.state.value.address)
    }

    @Test
    fun onReasonChanged_updates_reason() = runTest {
        val component = createComponent()

        component.onReasonChanged("Necesito visita domiciliaria")

        assertEquals("Necesito visita domiciliaria", component.state.value.reason)
    }

    @Test
    fun onSubmit_without_visitType_does_not_emit() = runTest {
        val outputs = mutableListOf<ChangeVisitTypeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onSubmit()

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onSubmit_success_emits_Requested_output() = runTest {
        val outputs = mutableListOf<ChangeVisitTypeComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onNewVisitTypeSelected(VisitType.HOME)
        component.onAddressChanged("Av. Javier Prado 123")

        component.onSubmit()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ChangeVisitTypeComponent.Output.Requested)
    }

    @Test
    fun onSubmit_failure_sets_error_and_clears_isSubmitting() = runTest {
        val ds = FakeChangeVisitTypeDataSource(changeResult = Result.failure(Exception("Too late to change")))
        val outputs = mutableListOf<ChangeVisitTypeComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onNewVisitTypeSelected(VisitType.CLINIC)

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isSubmitting)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onSubmit_passes_null_address_when_blank() = runTest {
        val outputs = mutableListOf<ChangeVisitTypeComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onNewVisitTypeSelected(VisitType.VIRTUAL)

        component.onSubmit()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ChangeVisitTypeComponent.Output.Requested)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<ChangeVisitTypeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ChangeVisitTypeComponent.Output.Back)
    }
}
