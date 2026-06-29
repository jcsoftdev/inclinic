@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.RescheduleStatus
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private class FakeProposalDataSource : AppointmentDataSource {
    var proposalResult: Result<RescheduleProposal?> = Result.success(null)
    var lastAppointmentId: String? = null
    var callCount = 0

    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> {
        callCount++
        lastAppointmentId = appointmentId
        return proposalResult
    }

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class GetRescheduleProposalUseCaseTest {

    private val fake = FakeProposalDataSource()
    private val useCase = GetRescheduleProposalUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun returns_proposal_when_exists() = runTest {
        val now = Clock.System.now()
        val proposal = RescheduleProposal(
            id = "rp-1", appointmentId = "apt-1", requestedBy = "doctor",
            proposedStart = now + 5.days, proposedEnd = now + 5.days + 1.hours,
            reason = "Conflict", status = RescheduleStatus.PENDING,
            expiresAt = now + 2.days, createdAt = now,
        )
        fake.proposalResult = Result.success(proposal)

        val result = useCase("apt-1")

        assertTrue(result.isSuccess)
        assertEquals("rp-1", result.getOrNull()?.id)
        assertEquals("apt-1", fake.lastAppointmentId)
    }

    @Test
    fun returns_null_when_no_proposal() = runTest {
        fake.proposalResult = Result.success(null)

        val result = useCase("apt-2")

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.proposalResult = Result.failure(Exception("Network"))

        val result = useCase("apt-1")

        assertTrue(result.isFailure)
    }
}
