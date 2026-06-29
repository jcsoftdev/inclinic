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
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

private class FakeRescheduleDataSource : AppointmentDataSource {
    var rescheduleResult: Result<Appointment> = Result.success(testAppointment())
    var lastAppointmentId: String? = null
    var lastDate: String? = null
    var lastSlotId: String? = null
    var callCount = 0

    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> {
        callCount++
        lastAppointmentId = appointmentId
        lastDate = date
        lastSlotId = slotId
        return rescheduleResult
    }

    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun processPackagePayment(cardToken: String, paymentMethodId: String, therapyPackageId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

private fun testAppointment(): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = AppointmentStatus.CONFIRMED,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now + 72.hours, endsAt = now + 73.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

class RescheduleAppointmentUseCaseTest {

    private val fake = FakeRescheduleDataSource()
    private val useCase = RescheduleAppointmentUseCase(dataSource = fake, dispatchers = TestAppDispatchers())

    @Test
    fun success_forwards_args_and_returns_appointment() = runTest {
        val result = useCase("apt-1", "2026-06-01", "slot-3")

        assertTrue(result.isSuccess)
        assertEquals("apt-1", result.getOrNull()?.id)
        assertEquals(1, fake.callCount)
        assertEquals("apt-1", fake.lastAppointmentId)
        assertEquals("2026-06-01", fake.lastDate)
        assertEquals("slot-3", fake.lastSlotId)
    }

    @Test
    fun failure_propagates_error() = runTest {
        fake.rescheduleResult = Result.failure(Exception("Already rescheduled"))

        val result = useCase("apt-1", "2026-06-01", "slot-1")

        assertTrue(result.isFailure)
        assertEquals("Already rescheduled", result.exceptionOrNull()?.message)
    }
}
