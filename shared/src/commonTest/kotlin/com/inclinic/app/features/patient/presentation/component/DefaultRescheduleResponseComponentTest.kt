@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.RescheduleStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.appointments.application.GetRescheduleProposalUseCase
import com.inclinic.app.features.patient.appointments.application.RespondRescheduleUseCase
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

private fun testProposal(): RescheduleProposal {
    val now = Clock.System.now()
    return RescheduleProposal(
        id = "rp-1",
        appointmentId = "apt-1",
        requestedBy = "doctor",
        proposedStart = now + 48.hours,
        proposedEnd = now + 49.hours,
        reason = "Doctor unavailable original time",
        status = RescheduleStatus.PENDING,
        expiresAt = now + 24.hours,
        createdAt = now,
        doctorName = "Dr. Ana Torres",
        visitType = VisitType.VIRTUAL,
    )
}

private class FakeRescheduleResponseDataSource(
    private val proposalResult: Result<RescheduleProposal?> = Result.success(testProposal()),
    private val respondResult: Result<Unit> = Result.success(Unit),
) : AppointmentDataSource {
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = proposalResult
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = respondResult
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class DefaultRescheduleResponseComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: AppointmentDataSource = FakeRescheduleResponseDataSource(),
        outputs: MutableList<RescheduleResponseComponent.Output> = mutableListOf(),
    ): DefaultRescheduleResponseComponent {
        return DefaultRescheduleResponseComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getProposal = GetRescheduleProposalUseCase(dataSource, dispatchers),
            respondReschedule = RespondRescheduleUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_proposal_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.proposal)
        assertEquals("rp-1", state.proposal?.id)
        assertEquals("Dr. Ana Torres", state.proposal?.doctorName)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error() = runTest {
        val ds = FakeRescheduleResponseDataSource(proposalResult = Result.failure(Exception("Network error")))
        val component = createComponent(dataSource = ds)

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun load_null_proposal_sets_null_proposal_in_state() = runTest {
        val ds = FakeRescheduleResponseDataSource(proposalResult = Result.success(null))
        val component = createComponent(dataSource = ds)

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.proposal)
        assertNull(component.state.value.error)
    }

    @Test
    fun onAccept_success_emits_Responded_output() = runTest {
        val outputs = mutableListOf<RescheduleResponseComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onAccept()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is RescheduleResponseComponent.Output.Responded)
    }

    @Test
    fun onReject_success_emits_Responded_output() = runTest {
        val outputs = mutableListOf<RescheduleResponseComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onReject()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is RescheduleResponseComponent.Output.Responded)
    }

    @Test
    fun onAccept_without_proposal_does_not_emit() = runTest {
        val ds = FakeRescheduleResponseDataSource(proposalResult = Result.success(null))
        val outputs = mutableListOf<RescheduleResponseComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)

        component.onAccept()

        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onAccept_failure_sets_error_and_clears_isResponding() = runTest {
        val ds = FakeRescheduleResponseDataSource(respondResult = Result.failure(Exception("Expired")))
        val outputs = mutableListOf<RescheduleResponseComponent.Output>()
        val component = createComponent(dataSource = ds, outputs = outputs)

        component.onAccept()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isResponding)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<RescheduleResponseComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is RescheduleResponseComponent.Output.Back)
    }
}
