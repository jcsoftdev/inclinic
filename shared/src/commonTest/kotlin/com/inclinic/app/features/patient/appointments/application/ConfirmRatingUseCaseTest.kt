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

private class FakeRatingDataSource : AppointmentDataSource {
    var ratingResult: Result<Unit> = Result.success(Unit)
    var lastAppointmentId: String? = null
    var lastPunctuality: Int? = null
    var lastProfessionalism: Int? = null
    var lastEmpathy: Int? = null
    var lastComment: String? = null
    var callCount = 0

    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?): Result<Unit> {
        callCount++
        lastAppointmentId = appointmentId
        lastPunctuality = punctuality
        lastProfessionalism = professionalism
        lastEmpathy = empathy
        lastComment = comment
        return ratingResult
    }

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class ConfirmRatingUseCaseTest {

    private val fake = FakeRatingDataSource()
    private val useCase = ConfirmRatingUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_forwards_all_args_including_comment() = runTest {
        val result = useCase("apt-1", punctuality = 5, professionalism = 4, empathy = 5, comment = "Great doctor!")

        assertTrue(result.isSuccess)
        assertEquals(1, fake.callCount)
        assertEquals("apt-1", fake.lastAppointmentId)
        assertEquals(5, fake.lastPunctuality)
        assertEquals(4, fake.lastProfessionalism)
        assertEquals(5, fake.lastEmpathy)
        assertEquals("Great doctor!", fake.lastComment)
    }

    @Test
    fun success_with_null_comment() = runTest {
        val result = useCase("apt-2", punctuality = 3, professionalism = 3, empathy = 3, comment = null)

        assertTrue(result.isSuccess)
        assertNull(fake.lastComment)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.ratingResult = Result.failure(Exception("Server error"))

        val result = useCase("apt-1", 5, 5, 5, "Nice")

        assertTrue(result.isFailure)
        assertEquals("Server error", result.exceptionOrNull()?.message)
    }
}
