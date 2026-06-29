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
import com.inclinic.app.features.patient.appointments.application.ConfirmRatingUseCase
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
import kotlin.time.Duration.Companion.hours

private fun ratingTestAppointment(): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = AppointmentStatus.COMPLETED,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now - 2.hours, endsAt = now - 1.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now - 3.hours,
    )
}

private class FakeRatingAppointmentDataSource(
    private val appointment: Appointment? = ratingTestAppointment(),
    private val loadError: Throwable? = null,
    private val ratingResult: Result<Unit> = Result.success(Unit),
) : AppointmentDataSource {
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (loadError != null) Result.failure(loadError)
        else if (appointment != null) Result.success(appointment)
        else Result.failure(Exception("Not found"))

    override suspend fun confirmRating(
        appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?,
    ): Result<Unit> = ratingResult

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class DefaultConfirmRatingComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: AppointmentDataSource = FakeRatingAppointmentDataSource(),
        outputs: MutableList<ConfirmRatingComponent.Output> = mutableListOf(),
    ): DefaultConfirmRatingComponent {
        return DefaultConfirmRatingComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getAppointmentDetail = GetAppointmentDetailUseCase(dataSource, dispatchers),
            confirmRating = ConfirmRatingUseCase(dataSource, dispatchers),
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
        val component = createComponent(
            dataSource = FakeRatingAppointmentDataSource(loadError = Exception("Not found")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.appointment)
        assertEquals("Not found", state.error)
    }

    @Test
    fun onPunctualityChanged_clamps_to_1_5_range() = runTest {
        val component = createComponent()

        component.onPunctualityChanged(3)
        assertEquals(3, component.state.value.punctuality)

        component.onPunctualityChanged(0)
        assertEquals(1, component.state.value.punctuality)

        component.onPunctualityChanged(10)
        assertEquals(5, component.state.value.punctuality)
    }

    @Test
    fun onProfessionalismChanged_clamps_to_1_5_range() = runTest {
        val component = createComponent()

        component.onProfessionalismChanged(4)
        assertEquals(4, component.state.value.professionalism)

        component.onProfessionalismChanged(-1)
        assertEquals(1, component.state.value.professionalism)
    }

    @Test
    fun onEmpathyChanged_clamps_to_1_5_range() = runTest {
        val component = createComponent()

        component.onEmpathyChanged(5)
        assertEquals(5, component.state.value.empathy)

        component.onEmpathyChanged(6)
        assertEquals(5, component.state.value.empathy)
    }

    @Test
    fun onCommentChanged_updates_comment_in_state() = runTest {
        val component = createComponent()

        component.onCommentChanged("Excellent doctor")

        assertEquals("Excellent doctor", component.state.value.comment)
    }

    @Test
    fun onConfirm_with_zero_ratings_does_not_submit() = runTest {
        val outputs = mutableListOf<ConfirmRatingComponent.Output>()
        val component = createComponent(outputs = outputs)
        // ratings default to 0 — component should guard against this

        component.onConfirm()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isSubmitting)
    }

    @Test
    fun onConfirm_success_emits_Confirmed_output() = runTest {
        val outputs = mutableListOf<ConfirmRatingComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onPunctualityChanged(5)
        component.onProfessionalismChanged(4)
        component.onEmpathyChanged(5)

        component.onConfirm()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ConfirmRatingComponent.Output.Confirmed)
    }

    @Test
    fun onConfirm_failure_sets_error_and_clears_isSubmitting() = runTest {
        val outputs = mutableListOf<ConfirmRatingComponent.Output>()
        val component = createComponent(
            dataSource = FakeRatingAppointmentDataSource(ratingResult = Result.failure(Exception("Already rated"))),
            outputs = outputs,
        )
        component.onPunctualityChanged(5)
        component.onProfessionalismChanged(5)
        component.onEmpathyChanged(5)

        component.onConfirm()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isSubmitting)
        assertEquals("Already rated", component.state.value.error)
    }

    @Test
    fun onDispute_emits_NavigateToDispute_with_correct_appointmentId() = runTest {
        val outputs = mutableListOf<ConfirmRatingComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onDispute()

        assertEquals(1, outputs.size)
        val output = outputs.first() as ConfirmRatingComponent.Output.NavigateToDispute
        assertEquals("apt-1", output.appointmentId)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<ConfirmRatingComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ConfirmRatingComponent.Output.Back)
    }
}
