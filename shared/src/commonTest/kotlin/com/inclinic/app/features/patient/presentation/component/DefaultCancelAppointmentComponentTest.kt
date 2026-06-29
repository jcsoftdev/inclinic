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
import com.inclinic.app.features.patient.appointments.application.CancelAppointmentUseCase
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private fun cancelTestAppointment(
    daysFromNow: Int = 5,
    status: AppointmentStatus = AppointmentStatus.CONFIRMED,
): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = status,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now + daysFromNow.days, endsAt = now + daysFromNow.days + 1.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

private class FakeCancelAppointmentDataSource(
    private val appointment: Appointment? = cancelTestAppointment(),
    private val loadError: Throwable? = null,
    private val cancelResult: Result<Unit> = Result.success(Unit),
) : AppointmentDataSource {
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (loadError != null) Result.failure(loadError)
        else if (appointment != null) Result.success(appointment)
        else Result.failure(Exception("Not found"))

    override suspend fun cancelAppointment(appointmentId: String, reason: String): Result<Unit> = cancelResult

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class DefaultCancelAppointmentComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: AppointmentDataSource = FakeCancelAppointmentDataSource(),
        outputs: MutableList<CancelAppointmentComponent.Output> = mutableListOf(),
    ): DefaultCancelAppointmentComponent {
        return DefaultCancelAppointmentComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getAppointmentDetail = GetAppointmentDetailUseCase(dataSource, dispatchers),
            cancelAppointment = CancelAppointmentUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_appointment_and_canCancel_for_future_appointment() = runTest {
        val component = createComponent(
            dataSource = FakeCancelAppointmentDataSource(appointment = cancelTestAppointment(daysFromNow = 5)),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.appointment)
        assertEquals("apt-1", state.appointment?.id)
        assertTrue(state.canCancel)
        assertNull(state.error)
    }

    @Test
    fun load_success_sets_canCancel_false_when_less_than_3_days() = runTest {
        val component = createComponent(
            dataSource = FakeCancelAppointmentDataSource(appointment = cancelTestAppointment(daysFromNow = 1)),
        )

        val state = component.state.value
        assertFalse(state.canCancel)
    }

    @Test
    fun load_success_sets_canCancel_true_for_pending_payment_regardless_of_days() = runTest {
        val component = createComponent(
            dataSource = FakeCancelAppointmentDataSource(
                appointment = cancelTestAppointment(daysFromNow = 1, status = AppointmentStatus.PENDING_PAYMENT),
            ),
        )

        val state = component.state.value
        assertTrue(state.canCancel)
    }

    @Test
    fun load_failure_sets_error() = runTest {
        val component = createComponent(
            dataSource = FakeCancelAppointmentDataSource(loadError = Exception("Not found")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertNull(state.appointment)
    }

    @Test
    fun onReasonChanged_updates_reason_in_state() = runTest {
        val component = createComponent()

        component.onReasonChanged("Personal reasons")

        assertEquals("Personal reasons", component.state.value.reason)
    }

    @Test
    fun onConfirmCancel_success_emits_Cancelled_output() = runTest {
        val outputs = mutableListOf<CancelAppointmentComponent.Output>()
        val component = createComponent(
            dataSource = FakeCancelAppointmentDataSource(
                appointment = cancelTestAppointment(daysFromNow = 5),
                cancelResult = Result.success(Unit),
            ),
            outputs = outputs,
        )

        component.onConfirmCancel()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is CancelAppointmentComponent.Output.Cancelled)
    }

    @Test
    fun onConfirmCancel_failure_sets_error_and_clears_isCancelling() = runTest {
        val outputs = mutableListOf<CancelAppointmentComponent.Output>()
        val component = createComponent(
            dataSource = FakeCancelAppointmentDataSource(
                appointment = cancelTestAppointment(daysFromNow = 1),
                cancelResult = Result.failure(Exception("Cannot cancel")),
            ),
            outputs = outputs,
        )

        component.onConfirmCancel()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isCancelling)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<CancelAppointmentComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is CancelAppointmentComponent.Output.Back)
    }
}
