@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.reschedule_request.presentation.component

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.appointments.application.GetDoctorAppointmentDetailUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import com.inclinic.app.features.doctor.reschedule_request.application.RequestRescheduleUseCase
import com.inclinic.app.features.doctor.reschedule_request.core.port.RescheduleRequestRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun stubAppointment(): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = AppointmentStatus.CONFIRMED,
        consultationFee = 100.0, commissionAmount = 15.0,
        startsAt = now + 48.hours, endsAt = now + 49.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

private class FakeDetailDataSource(
    private val appointment: Appointment? = stubAppointment(),
    private val loadError: Throwable? = null,
) : DoctorAppointmentDataSource {
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (loadError != null) Result.failure(loadError) else Result.success(appointment!!)

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
    override suspend fun markNoShow(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())
}

private class FakeRescheduleRequestRepository : RescheduleRequestRepository {
    var result: Result<Appointment> = Result.success(stubAppointment())
    var lastAppointmentId: String? = null
    var lastProposedSlot: String? = null
    var lastMessage: String? = null
    var callCount = 0

    override suspend fun requestReschedule(
        appointmentId: String,
        proposedSlot: String,
        message: String?,
    ): Result<Appointment> {
        callCount++
        lastAppointmentId = appointmentId
        lastProposedSlot = proposedSlot
        lastMessage = message
        return result
    }
}

class DefaultRequestRescheduleComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        detailDataSource: FakeDetailDataSource = FakeDetailDataSource(),
        repository: FakeRescheduleRequestRepository = FakeRescheduleRequestRepository(),
        outputs: MutableList<RequestRescheduleComponent.Output> = mutableListOf(),
    ): DefaultRequestRescheduleComponent =
        DefaultRequestRescheduleComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getAppointmentDetail = GetDoctorAppointmentDetailUseCase(detailDataSource, dispatchers),
            requestReschedule = RequestRescheduleUseCase(repository, dispatchers),
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
        val ds = FakeDetailDataSource(appointment = null, loadError = Exception("Network error"))
        val component = createComponent(detailDataSource = ds)

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.appointment)
        assertEquals("Network error", state.error)
    }

    @Test
    fun onSlotChange_updates_proposedSlot_via_turbine() = runTest {
        val component = createComponent()

        component.state.asFlow().test {
            assertEquals("", awaitItem().proposedSlot)
            component.onSlotChange("2026-06-01T10:30:00Z")
            assertEquals("2026-06-01T10:30:00Z", awaitItem().proposedSlot)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun canSubmit_false_when_slot_blank() = runTest {
        val component = createComponent()
        component.onMessageChange("Necesito mover esta cita por motivos personales")
        assertFalse(component.state.value.canSubmit)
    }

    @Test
    fun canSubmit_false_when_message_shorter_than_min() = runTest {
        val component = createComponent()
        component.onSlotChange("2026-06-01T10:30:00Z")
        component.onMessageChange("muy corto")
        assertFalse(component.state.value.canSubmit)
    }

    @Test
    fun canSubmit_true_when_slot_present_and_message_meets_min() = runTest {
        val component = createComponent()
        component.onSlotChange("2026-06-01T10:30:00Z")
        component.onMessageChange("Necesito mover esta cita por un imprevisto")
        assertTrue(component.state.value.canSubmit)
    }

    @Test
    fun onSubmit_with_blank_slot_does_nothing() = runTest {
        val repo = FakeRescheduleRequestRepository()
        val outputs = mutableListOf<RequestRescheduleComponent.Output>()
        val component = createComponent(repository = repo, outputs = outputs)

        component.onSubmit()

        assertEquals(0, repo.callCount)
        assertTrue(outputs.isEmpty())
    }

    @Test
    fun onSubmit_success_forwards_args_and_emits_Success() = runTest {
        val repo = FakeRescheduleRequestRepository()
        val outputs = mutableListOf<RequestRescheduleComponent.Output>()
        val component = createComponent(repository = repo, outputs = outputs)
        component.onSlotChange("2026-06-01T10:30:00Z")
        component.onMessageChange("Tengo una emergencia familiar y necesito moverla")

        component.onSubmit()

        assertEquals(1, repo.callCount)
        assertEquals("apt-1", repo.lastAppointmentId)
        assertEquals("2026-06-01T10:30:00Z", repo.lastProposedSlot)
        assertEquals("Tengo una emergencia familiar y necesito moverla", repo.lastMessage)
        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is RequestRescheduleComponent.Output.Success)
        assertFalse(component.state.value.isSubmitting)
    }

    @Test
    fun onSubmit_forwards_trimmed_message() = runTest {
        val repo = FakeRescheduleRequestRepository()
        val component = createComponent(repository = repo)
        component.onSlotChange("2026-06-01T10:30:00Z")
        component.onMessageChange("  Necesito mover esta cita por un imprevisto  ")

        component.onSubmit()

        assertEquals("Necesito mover esta cita por un imprevisto", repo.lastMessage)
    }

    @Test
    fun onSubmit_failure_sets_error_and_clears_isSubmitting() = runTest {
        val repo = FakeRescheduleRequestRepository()
        repo.result = Result.failure(Exception("Server error"))
        val outputs = mutableListOf<RequestRescheduleComponent.Output>()
        val component = createComponent(repository = repo, outputs = outputs)
        component.onSlotChange("2026-06-01T10:30:00Z")
        component.onMessageChange("Necesito mover esta cita por un imprevisto")

        component.onSubmit()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isSubmitting)
        assertEquals("Server error", component.state.value.error)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<RequestRescheduleComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is RequestRescheduleComponent.Output.Back)
    }
}

private fun <T : Any> Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { trySend(it) }
    awaitClose { cancellation.cancel() }
}
