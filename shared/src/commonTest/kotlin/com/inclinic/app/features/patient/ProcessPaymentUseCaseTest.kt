@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.CardToken
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.port.CardTokenizer
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import com.inclinic.app.features.patient.payment.application.ProcessPaymentUseCase
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// --- Fakes ---

private class FakeCardTokenizer : CardTokenizer {
    var result: Result<CardToken> = Result.success(CardToken(token = "tok_test", last4 = "1111", brand = "visa"))
    var callCount = 0

    override suspend fun tokenize(card: RawCard): Result<CardToken> {
        callCount++
        return result
    }
}

private class FakeAppointmentDataSource : AppointmentDataSource {
    var paymentResult: Result<PaymentResult> = Result.success(
        PaymentResult(appointmentId = "apt-1", status = "approved", transactionId = "txn-1")
    )
    var processPaymentCallCount = 0

    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> {
        processPaymentCallCount++
        return paymentResult
    }

    override suspend fun getAvailability(doctorId: String, date: String): Result<List<AvailabilitySlot>> =
        Result.success(emptyList())

    override suspend fun createAppointment(
        doctorId: String, date: String, slotId: String, visitType: String, notes: String?,
    ): Result<Appointment> = Result.failure(UnsupportedOperationException())

    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int): Result<List<Appointment>> =
        Result.success(emptyList())

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun cancelAppointment(appointmentId: String, reason: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getMonthAvailability(doctorId: String, month: String): Result<Map<String, String>> =
        Result.success(emptyMap())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> =
        Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<com.inclinic.app.core.model.RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

// --- Tests ---

class ProcessPaymentUseCaseTest {

    private val fakeTokenizer = FakeCardTokenizer()
    private val fakeDataSource = FakeAppointmentDataSource()
    private val dispatchers = TestAppDispatchers()

    private val useCase = ProcessPaymentUseCase(
        cardTokenizer = fakeTokenizer,
        dataSource = fakeDataSource,
        dispatchers = dispatchers,
    )

    private val validCard = RawCard(
        pan = "4111111111111111",
        cvv = "123",
        expMonth = 12,
        expYear = 2027,
        holderName = "Maria Lopez",
        docType = "DNI",
        docNumber = "12345678",
    )

    @Test
    fun tokenize_success_and_process_success_returns_PaymentResult() = runTest {
        fakeTokenizer.result = Result.success(CardToken(token = "tok_abc", last4 = "1111", brand = "visa"))
        fakeDataSource.paymentResult = Result.success(
            PaymentResult(appointmentId = "apt-1", status = "approved", transactionId = "txn-1")
        )

        val result = useCase(validCard, "apt-1")

        assertTrue(result.isSuccess)
        assertEquals("approved", result.getOrNull()?.status)
        assertEquals(1, fakeTokenizer.callCount)
        assertEquals(1, fakeDataSource.processPaymentCallCount)
    }

    @Test
    fun tokenize_failure_returns_failure_and_no_network_call_is_made() = runTest {
        fakeTokenizer.result = Result.failure(Exception("Tokenization failed"))

        val result = useCase(validCard, "apt-1")

        assertTrue(result.isFailure)
        assertEquals(1, fakeTokenizer.callCount)
        // processPayment must NOT be called when tokenization fails
        assertEquals(0, fakeDataSource.processPaymentCallCount)
    }

    @Test
    fun tokenize_success_then_process_failure_returns_failure() = runTest {
        fakeTokenizer.result = Result.success(CardToken(token = "tok_abc", last4 = "1111", brand = "visa"))
        fakeDataSource.paymentResult = Result.failure(Exception("Payment gateway error"))

        val result = useCase(validCard, "apt-1")

        assertTrue(result.isFailure)
        assertEquals(1, fakeTokenizer.callCount)
        assertEquals(1, fakeDataSource.processPaymentCallCount)
    }
}
