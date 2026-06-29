@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.reschedule_request.application

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.reschedule_request.core.port.RescheduleRequestRepository
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.test.Test
import kotlin.test.assertEquals
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

class RequestRescheduleUseCaseTest {

    private val repo = FakeRescheduleRequestRepository()
    private val useCase = RequestRescheduleUseCase(repo, TestAppDispatchers())

    @Test
    fun success_forwards_args_and_returns_appointment() = runTest {
        val result = useCase("apt-1", "2026-06-01T10:30:00Z", "Tengo una emergencia")

        assertTrue(result.isSuccess)
        assertEquals("apt-1", result.getOrNull()?.id)
        assertEquals(1, repo.callCount)
        assertEquals("apt-1", repo.lastAppointmentId)
        assertEquals("2026-06-01T10:30:00Z", repo.lastProposedSlot)
        assertEquals("Tengo una emergencia", repo.lastMessage)
    }

    @Test
    fun passes_null_message_to_repository() = runTest {
        useCase("apt-9", "2026-06-02T14:00:00Z", null)
        assertEquals("apt-9", repo.lastAppointmentId)
        assertEquals(null, repo.lastMessage)
    }

    @Test
    fun failure_propagates_error() = runTest {
        repo.result = Result.failure(RuntimeException("Already rescheduled"))

        val result = useCase("apt-1", "2026-06-01T10:30:00Z", null)

        assertTrue(result.isFailure)
        assertEquals("Already rescheduled", result.exceptionOrNull()?.message)
    }
}
