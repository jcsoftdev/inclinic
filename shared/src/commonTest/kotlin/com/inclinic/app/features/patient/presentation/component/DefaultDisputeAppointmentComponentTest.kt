@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.DisputeReason
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.appointments.application.DisputeAppointmentUseCase
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
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

private fun disputeTestAppointment(): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = AppointmentStatus.COMPLETED,
        consultationFee = 100.0, commissionAmount = 15.0,
        startsAt = now - 2.hours, endsAt = now - 1.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now - 3.hours,
    )
}

private class FakeDisputeAppointmentDataSource(
    private val appointment: Appointment? = disputeTestAppointment(),
    private val loadError: Throwable? = null,
    private var disputeResult: Result<Unit> = Result.success(Unit),
) : AppointmentDataSource {
    var disputeCallCount = 0
    var lastDisputeReason: String? = null
    var lastDisputeDetails: String? = null

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (loadError != null) Result.failure(loadError) else Result.success(appointment!!)

    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String, attachments: List<String>): Result<Unit> {
        disputeCallCount++
        lastDisputeReason = reason
        lastDisputeDetails = details
        return disputeResult
    }

    fun setDisputeResult(result: Result<Unit>) { disputeResult = result }

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class DefaultDisputeAppointmentComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: FakeDisputeAppointmentDataSource = FakeDisputeAppointmentDataSource(),
        outputs: MutableList<DisputeAppointmentComponent.Output> = mutableListOf(),
    ): DefaultDisputeAppointmentComponent {
        return DefaultDisputeAppointmentComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getAppointmentDetail = GetAppointmentDetailUseCase(dataSource, dispatchers),
            disputeAppointment = DisputeAppointmentUseCase(dataSource, dispatchers),
            uploadFile = com.inclinic.app.core.upload.UploadFileUseCase(com.inclinic.app.core.upload.FakeUploadDataSource(), dispatchers),
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
    fun load_failure_sets_error_in_state() = runTest {
        val ds = FakeDisputeAppointmentDataSource(appointment = null, loadError = Exception("Network error"))
        val component = createComponent(dataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.appointment)
        assertNotNull(state.error)
    }

    @Test
    fun onReasonSelected_updates_selectedReason() = runTest {
        val component = createComponent()

        component.onReasonSelected(DisputeReason.DOCTOR_NO_SHOW)

        assertEquals(DisputeReason.DOCTOR_NO_SHOW, component.state.value.selectedReason)
    }

    @Test
    fun onDetailsChanged_updates_details_and_truncates_at_300_chars() = runTest {
        val component = createComponent()
        val longText = "x".repeat(400)

        component.onDetailsChanged(longText)

        assertEquals(300, component.state.value.details.length)
    }

    @Test
    fun onSubmit_without_reason_does_nothing() = runTest {
        val ds = FakeDisputeAppointmentDataSource()
        val outputs = mutableListOf<DisputeAppointmentComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onDetailsChanged("some details")

        component.onSubmit()

        assertEquals(0, ds.disputeCallCount)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onSubmit_without_details_does_nothing() = runTest {
        val ds = FakeDisputeAppointmentDataSource()
        val outputs = mutableListOf<DisputeAppointmentComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onReasonSelected(DisputeReason.INADEQUATE_SERVICE)

        component.onSubmit()

        assertEquals(0, ds.disputeCallCount)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onSubmit_success_emits_Disputed_output() = runTest {
        val ds = FakeDisputeAppointmentDataSource()
        val outputs = mutableListOf<DisputeAppointmentComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onReasonSelected(DisputeReason.INCORRECT_CHARGE)
        component.onDetailsChanged("Charged twice for the same consultation")

        component.onSubmit()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is DisputeAppointmentComponent.Output.Disputed)
        assertEquals("INCORRECT_CHARGE", ds.lastDisputeReason)
    }

    @Test
    fun onSubmit_failure_sets_error_and_clears_isSubmitting() = runTest {
        val ds = FakeDisputeAppointmentDataSource()
        ds.setDisputeResult(Result.failure(Exception("Server error")))
        val outputs = mutableListOf<DisputeAppointmentComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)
        component.onReasonSelected(DisputeReason.DOCTOR_NO_SHOW)
        component.onDetailsChanged("Doctor never showed up")

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isSubmitting)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<DisputeAppointmentComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is DisputeAppointmentComponent.Output.Back)
    }
}
