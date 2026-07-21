@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeDisputeDataSource : AppointmentDataSource {
    var disputeResult: Result<Unit> = Result.success(Unit)
    var lastAppointmentId: String? = null
    var lastReason: String? = null
    var lastDetails: String? = null
    var callCount = 0

    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String, attachments: List<String>): Result<Unit> {
        callCount++
        lastAppointmentId = appointmentId
        lastReason = reason
        lastDetails = details
        return disputeResult
    }

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class DisputeAppointmentUseCaseTest {

    private val fake = FakeDisputeDataSource()
    private val useCase = DisputeAppointmentUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_forwards_args_and_returns_success() = runTest {
        val result = useCase("apt-1", "Wrong diagnosis", "Doctor was rude")

        assertTrue(result.isSuccess)
        assertEquals(1, fake.callCount)
        assertEquals("apt-1", fake.lastAppointmentId)
        assertEquals("Wrong diagnosis", fake.lastReason)
        assertEquals("Doctor was rude", fake.lastDetails)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.disputeResult = Result.failure(Exception("Network error"))

        val result = useCase("apt-2", "Reason", "Details")

        assertTrue(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
        assertEquals(1, fake.callCount)
    }
}
