@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.DoctorPlan
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.core.model.PaymentResult
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.core.model.Review
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import com.inclinic.app.features.patient.infrastructure.remote.DoctorFilters
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PagedDoctors
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

private fun testAppointment(visitType: VisitType = VisitType.VIRTUAL): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = visitType, status = AppointmentStatus.CONFIRMED,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now + 72.hours, endsAt = now + 73.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

private fun testDoctor(): Doctor = Doctor(
    id = "doc-1", fullName = "Dr. Ana Torres", email = "ana@test.com", photoUrl = null,
    specialties = emptyList(), plan = DoctorPlan.FREE, ratingAverage = 4.5, ratingsCount = 10,
    consultationFee = 120.0, homeVisitAvailable = false, virtualVisitAvailable = true,
    bio = "Especialista", isVerified = true, cmpLicense = "CMP-123",
)

private class FakeAppointmentDataSource(
    private val appointment: Appointment? = testAppointment(),
    private val error: Throwable? = null,
) : AppointmentDataSource {
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> =
        if (error != null) Result.failure(error) else Result.success(appointment!!)

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
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String, attachments: List<String>) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

private class FakeDoctorSearchDataSource(
    private val doctor: Doctor? = testDoctor(),
) : DoctorSearchDataSource {
    override suspend fun getDoctorById(doctorId: String): Result<Doctor> =
        if (doctor != null) Result.success(doctor) else Result.failure(Exception("Not found"))

    override suspend fun searchDoctors(filters: DoctorFilters, page: Int) =
        Result.success(PagedDoctors(emptyList(), false))

    override suspend fun getDoctorReviews(doctorId: String, page: Int) = Result.success(emptyList<Review>())
}

class DefaultAppointmentDetailComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        apptDataSource: AppointmentDataSource = FakeAppointmentDataSource(),
        doctorDataSource: DoctorSearchDataSource = FakeDoctorSearchDataSource(),
        outputs: MutableList<AppointmentDetailComponent.Output> = mutableListOf(),
    ): DefaultAppointmentDetailComponent {
        return DefaultAppointmentDetailComponent(
            componentContext = ctx,
            appointmentId = "apt-1",
            getAppointmentDetail = GetAppointmentDetailUseCase(apptDataSource, dispatchers),
            getDoctorDetail = GetDoctorDetailUseCase(doctorDataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_appointment_and_doctor_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.appointment)
        assertEquals("apt-1", state.appointment?.id)
        assertNotNull(state.doctor)
        assertEquals("Dr. Ana Torres", state.doctor?.fullName)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val component = createComponent(
            apptDataSource = FakeAppointmentDataSource(appointment = null, error = Exception("Network error")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNull(state.appointment)
        assertNotNull(state.error)
    }

    @Test
    fun onPayNow_emits_NavigateToPayment_output() = runTest {
        val outputs = mutableListOf<AppointmentDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onPayNow()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is AppointmentDetailComponent.Output.NavigateToPayment)
        assertEquals("apt-1", (output as AppointmentDetailComponent.Output.NavigateToPayment).appointmentId)
    }

    @Test
    fun onCancel_emits_NavigateToCancel_output() = runTest {
        val outputs = mutableListOf<AppointmentDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onCancel()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is AppointmentDetailComponent.Output.NavigateToCancel)
    }

    @Test
    fun onReschedule_emits_NavigateToReschedule_with_correct_consultType() = runTest {
        val outputs = mutableListOf<AppointmentDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onReschedule()

        assertEquals(1, outputs.size)
        val output = outputs.first() as AppointmentDetailComponent.Output.NavigateToReschedule
        assertEquals("apt-1", output.appointmentId)
        assertEquals("doc-1", output.doctorId)
        assertEquals("telemedicine", output.consultType)
    }

    @Test
    fun onReschedule_home_visit_maps_to_home() = runTest {
        val outputs = mutableListOf<AppointmentDetailComponent.Output>()
        val component = createComponent(
            apptDataSource = FakeAppointmentDataSource(appointment = testAppointment(VisitType.HOME)),
            outputs = outputs,
        )

        component.onReschedule()

        val output = outputs.first() as AppointmentDetailComponent.Output.NavigateToReschedule
        assertEquals("home", output.consultType)
    }

    @Test
    fun onChat_emits_NavigateToChat_output() = runTest {
        val outputs = mutableListOf<AppointmentDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onChat()

        assertTrue(outputs.first() is AppointmentDetailComponent.Output.NavigateToChat)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<AppointmentDetailComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertTrue(outputs.first() is AppointmentDetailComponent.Output.Back)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val component = createComponent(
            apptDataSource = FakeAppointmentDataSource(appointment = null, error = Exception("Fail")),
        )
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }
}
