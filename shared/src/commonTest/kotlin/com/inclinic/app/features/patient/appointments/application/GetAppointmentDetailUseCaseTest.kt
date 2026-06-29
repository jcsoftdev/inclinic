@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

private class FakeGetDetailDataSource : AppointmentDataSource {
    var getByIdResult: Result<Appointment> = Result.success(testAppointment("apt-1"))
    var lastId: String? = null
    var callCount = 0

    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> {
        callCount++
        lastId = appointmentId
        return getByIdResult
    }

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

private fun testAppointment(id: String): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = id, doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.CLINIC, status = AppointmentStatus.CONFIRMED,
        consultationFee = 150.0, commissionAmount = 22.5,
        startsAt = now + 48.hours, endsAt = now + 49.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = "Checkup", createdAt = now,
    )
}

class GetAppointmentDetailUseCaseTest {

    private val fake = FakeGetDetailDataSource()
    private val useCase = GetAppointmentDetailUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_returns_appointment() = runTest {
        val result = useCase("apt-1")

        assertTrue(result.isSuccess)
        assertEquals("apt-1", result.getOrNull()?.id)
        assertEquals("apt-1", fake.lastId)
        assertEquals(1, fake.callCount)
    }

    @Test
    fun failure_propagates_not_found() = runTest {
        fake.getByIdResult = Result.failure(Exception("Not found"))

        val result = useCase("apt-999")

        assertTrue(result.isFailure)
        assertEquals("Not found", result.exceptionOrNull()?.message)
    }
}
