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
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeRespondRescheduleDataSource : AppointmentDataSource {
    var respondResult: Result<Unit> = Result.success(Unit)
    var lastRequestId: String? = null
    var lastAccept: Boolean? = null
    var lastNote: String? = null
    var callCount = 0

    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?): Result<Unit> {
        callCount++
        lastRequestId = requestId
        lastAccept = accept
        lastNote = responseNote
        return respondResult
    }

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?, homeVisitAddress: String?, homeVisitLat: Double?, homeVisitLng: Double?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String, attachments: List<String>) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class RespondRescheduleUseCaseTest {

    private val fake = FakeRespondRescheduleDataSource()
    private val useCase = RespondRescheduleUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun accept_forwards_true_and_note() = runTest {
        val result = useCase("req-1", accept = true, responseNote = "OK, works for me")

        assertTrue(result.isSuccess)
        assertEquals("req-1", fake.lastRequestId)
        assertEquals(true, fake.lastAccept)
        assertEquals("OK, works for me", fake.lastNote)
    }

    @Test
    fun reject_forwards_false_and_null_note() = runTest {
        val result = useCase("req-2", accept = false, responseNote = null)

        assertTrue(result.isSuccess)
        assertEquals(false, fake.lastAccept)
        assertNull(fake.lastNote)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.respondResult = Result.failure(Exception("Expired"))

        val result = useCase("req-1", accept = true, responseNote = null)

        assertTrue(result.isFailure)
        assertEquals("Expired", result.exceptionOrNull()?.message)
    }
}
