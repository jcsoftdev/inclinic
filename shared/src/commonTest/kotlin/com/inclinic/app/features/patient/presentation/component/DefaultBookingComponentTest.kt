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
import com.inclinic.app.features.patient.booking.application.CreateAppointmentUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import com.inclinic.app.features.patient.infrastructure.remote.DoctorFilters
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PagedDoctors
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun testAppointment(): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = AppointmentStatus.CONFIRMED,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now + 24.hours, endsAt = now + 25.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

private fun testDoctor(): Doctor = Doctor(
    id = "doc-1", fullName = "Dr. Ana Torres", email = "ana@test.com", photoUrl = null,
    specialties = emptyList(), plan = DoctorPlan.FREE, ratingAverage = 4.5, ratingsCount = 10,
    consultationFee = 120.0, homeVisitAvailable = false, virtualVisitAvailable = true,
    bio = "Especialista", isVerified = true, cmpLicense = "CMP-123",
)

private class FakeBookingAppointmentDataSource(
    private val createResult: Result<Appointment> = Result.success(testAppointment()),
) : AppointmentDataSource {
    override suspend fun getAvailability(doctorId: String, date: String) = Result.success(emptyList<AvailabilitySlot>())
    override suspend fun getMonthAvailability(doctorId: String, month: String) = Result.success(emptyMap<String, String>())
    override suspend fun createAppointment(doctorId: String, date: String, slotId: String, visitType: String, notes: String?): Result<Appointment> = createResult
    override suspend fun getPatientAppointments(patientId: String, status: String?, page: Int) = Result.success(emptyList<Appointment>())
    override suspend fun getAppointmentById(appointmentId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun cancelAppointment(appointmentId: String, reason: String) = Result.success(Unit)
    override suspend fun rescheduleAppointment(appointmentId: String, date: String, slotId: String): Result<Appointment> = Result.failure(UnsupportedOperationException())
    override suspend fun processPayment(cardToken: String, paymentMethodId: String, appointmentId: String): Result<PaymentResult> = Result.failure(UnsupportedOperationException())
    override suspend fun getPendingRescheduleProposal(appointmentId: String): Result<RescheduleProposal?> = Result.success(null)
    override suspend fun respondRescheduleProposal(requestId: String, accept: Boolean, responseNote: String?) = Result.success(Unit)
    override suspend fun disputeAppointment(appointmentId: String, reason: String, details: String) = Result.success(Unit)
    override suspend fun confirmRating(appointmentId: String, punctuality: Int, professionalism: Int, empathy: Int, comment: String?) = Result.success(Unit)
    override suspend fun requestVisitTypeChange(appointmentId: String, newVisitType: String, address: String?, reason: String?) = Result.success(Unit)
}

private class FakeBookingDoctorDataSource(
    private val doctor: Doctor? = testDoctor(),
) : DoctorSearchDataSource {
    override suspend fun getDoctorById(doctorId: String): Result<Doctor> =
        if (doctor != null) Result.success(doctor) else Result.failure(Exception("Not found"))
    override suspend fun searchDoctors(filters: DoctorFilters, page: Int) =
        Result.success(PagedDoctors(emptyList(), false))
    override suspend fun getDoctorReviews(doctorId: String, page: Int) = Result.success(emptyList<Review>())
}

class DefaultBookingComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        apptDataSource: AppointmentDataSource = FakeBookingAppointmentDataSource(),
        doctorDataSource: DoctorSearchDataSource = FakeBookingDoctorDataSource(),
        consultType: String = "telemedicine",
        outputs: MutableList<BookingComponent.Output> = mutableListOf(),
    ): DefaultBookingComponent {
        return DefaultBookingComponent(
            componentContext = ctx,
            doctorId = "doc-1",
            slotId = "slot-1",
            date = "2026-06-01",
            getDoctorDetail = GetDoctorDetailUseCase(doctorDataSource, dispatchers),
            createAppointment = CreateAppointmentUseCase(apptDataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
            consultType = consultType,
        )
    }

    @Test
    fun initial_state_loads_doctor_on_create() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertNotNull(state.doctor)
        assertEquals("Dr. Ana Torres", state.doctor?.fullName)
        assertNull(state.error)
    }

    @Test
    fun initial_visitType_from_consultType_telemedicine_is_VIRTUAL() = runTest {
        val component = createComponent(consultType = "telemedicine")

        assertEquals(VisitType.VIRTUAL, component.state.value.visitType)
    }

    @Test
    fun initial_visitType_from_consultType_home_is_HOME() = runTest {
        val component = createComponent(consultType = "home")

        assertEquals(VisitType.HOME, component.state.value.visitType)
    }

    @Test
    fun initial_visitType_from_consultType_office_is_CLINIC() = runTest {
        val component = createComponent(consultType = "office")

        assertEquals(VisitType.CLINIC, component.state.value.visitType)
    }

    @Test
    fun onVisitTypeChange_updates_visitType_and_clears_error() = runTest {
        val component = createComponent()

        component.onVisitTypeChange(VisitType.HOME)

        assertEquals(VisitType.HOME, component.state.value.visitType)
        assertNull(component.state.value.visitTypeError)
    }

    @Test
    fun onNotesChange_updates_notes() = runTest {
        val component = createComponent()

        component.onNotesChange("Tengo alergia al polvo")

        assertEquals("Tengo alergia al polvo", component.state.value.notes)
    }

    @Test
    fun onConfirm_success_emits_NavigateToPayment() = runTest {
        val outputs = mutableListOf<BookingComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onConfirm()

        assertEquals(1, outputs.size)
        val output = outputs.first()
        assertTrue(output is BookingComponent.Output.NavigateToPayment)
        assertEquals("apt-1", (output as BookingComponent.Output.NavigateToPayment).appointmentId)
    }

    @Test
    fun onConfirm_failure_sets_error_and_clears_isLoading() = runTest {
        val apptDs = FakeBookingAppointmentDataSource(createResult = Result.failure(Exception("Slot taken")))
        val outputs = mutableListOf<BookingComponent.Output>()
        val component = createComponent(apptDataSource = apptDs, outputs = outputs)

        component.onConfirm()

        assertTrue(outputs.isEmpty())
        assertFalse(component.state.value.isLoading)
        assertEquals("Slot taken", component.state.value.error)
    }

    @Test
    fun onSkipPayment_success_emits_NavigateToAppointments() = runTest {
        val outputs = mutableListOf<BookingComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onSkipPayment()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is BookingComponent.Output.NavigateToAppointments)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<BookingComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is BookingComponent.Output.Back)
    }

    @Test
    fun doctor_load_failure_sets_error() = runTest {
        val failingDs = FakeBookingDoctorDataSource(doctor = null)
        val component = createComponent(doctorDataSource = failingDs)

        assertNotNull(component.state.value.error)
        assertEquals("Not found", component.state.value.error)
    }
}
