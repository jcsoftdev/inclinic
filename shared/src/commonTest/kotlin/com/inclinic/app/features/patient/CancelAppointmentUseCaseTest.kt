@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.appointments.application.CancelAppointmentUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private class FakeCancelDataSource : AppointmentDataSource {
    var cancelResult: Result<Unit> = Result.success(Unit)
    var cancelCallCount = 0

    override suspend fun cancelAppointment(appointmentId: String, reason: String): Result<Unit> {
        cancelCallCount++
        return cancelResult
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

    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> =
        Result.failure(UnsupportedOperationException())

    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> =
        Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> =
        Result.failure(UnsupportedOperationException())

    override suspend fun getMonthAvailability(doctorId: String, month: String): Result<Map<String, String>> =
        Result.success(emptyMap())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<com.inclinic.app.core.model.RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String, attachments: List<String>) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

class CancelAppointmentUseCaseTest {

    private val fakeDataSource = FakeCancelDataSource()
    private val dispatchers = TestAppDispatchers()

    private val useCase = CancelAppointmentUseCase(
        dataSource = fakeDataSource,
        dispatchers = dispatchers,
    )

    @Test
    fun appointment_3_days_away_succeeds_and_calls_api() = runTest {
        val startsAt = Clock.System.now() + 3.days + 1.hours

        val result = useCase("apt-1", startsAt, "Change of plans")

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.cancelCallCount)
    }

    @Test
    fun appointment_7_days_away_succeeds() = runTest {
        val startsAt = Clock.System.now() + 7.days

        val result = useCase("apt-1", startsAt, "Doctor unavailable")

        assertTrue(result.isSuccess)
        assertEquals(1, fakeDataSource.cancelCallCount)
    }

    @Test
    fun appointment_less_than_3_days_away_returns_failure_without_api_call() = runTest {
        val startsAt = Clock.System.now() + 2.days

        val result = useCase("apt-1", startsAt, "Emergency")

        assertTrue(result.isFailure)
        // Guard must short-circuit before calling API
        assertEquals(0, fakeDataSource.cancelCallCount)
    }

    @Test
    fun appointment_tomorrow_returns_failure() = runTest {
        val startsAt = Clock.System.now() + 1.days

        val result = useCase("apt-1", startsAt, "Can't make it")

        assertTrue(result.isFailure)
        assertEquals(0, fakeDataSource.cancelCallCount)
    }

    @Test
    fun appointment_in_past_returns_failure() = runTest {
        val startsAt = Clock.System.now() - 1.days

        val result = useCase("apt-1", startsAt, "Already done")

        assertTrue(result.isFailure)
        assertEquals(0, fakeDataSource.cancelCallCount)
    }
}
