@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.DoctorPlan
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.home.application.GetPatientDashboardUseCase
import com.inclinic.app.features.patient.infrastructure.remote.PatientDashboard
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import com.inclinic.app.core.model.PatientProfile
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

private fun homeTestDoctor(): Doctor = Doctor(
    id = "doc-1", fullName = "Dr. Ana Torres", email = "ana@test.com", photoUrl = null,
    specialties = emptyList(), plan = DoctorPlan.FREE, ratingAverage = 4.5, ratingsCount = 10,
    consultationFee = 120.0, homeVisitAvailable = false, virtualVisitAvailable = true,
    bio = "Especialista", isVerified = true, cmpLicense = "CMP-123",
)

private fun homeTestAppointment(): Appointment {
    val now = Clock.System.now()
    return Appointment(
        id = "apt-1", doctorId = "doc-1", patientId = "pat-1", specialtyId = "sp-1",
        visitType = VisitType.VIRTUAL, status = AppointmentStatus.CONFIRMED,
        consultationFee = 120.0, commissionAmount = 18.0,
        startsAt = now + 24.hours, endsAt = now + 25.hours,
        rescheduleCount = 0, paymentDeadline = null, notes = null, createdAt = now,
    )
}

private class FakeHomePatientDataSource(
    private val dashboard: PatientDashboard = PatientDashboard(
        upcomingCount = 3,
        recentDoctors = listOf(homeTestDoctor()),
        nextAppointment = homeTestAppointment(),
    ),
    private val error: Throwable? = null,
) : PatientDataSource {
    override suspend fun getDashboard(patientId: String): Result<PatientDashboard> =
        if (error != null) Result.failure(error) else Result.success(dashboard)

    override suspend fun getPatientProfile(patientId: String): Result<PatientProfile> =
        Result.failure(UnsupportedOperationException())

    override suspend fun updatePatientProfile(
        patientId: String, name: String, phone: String?, dateOfBirth: String?,
    ): Result<PatientProfile> = Result.failure(UnsupportedOperationException())

    override suspend fun getMedicalProfile(patientId: String): Result<MedicalProfile> =
        Result.success(MedicalProfile.empty())

    override suspend fun updateMedicalProfile(patientId: String, profile: MedicalProfile): Result<MedicalProfile> =
        Result.success(profile)

    override suspend fun deleteAccount(password: String, reason: String?): Result<Unit> =
        Result.success(Unit)
}

class DefaultPatientHomeComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        dataSource: PatientDataSource = FakeHomePatientDataSource(),
        outputs: MutableList<PatientHomeComponent.Output> = mutableListOf(),
    ): DefaultPatientHomeComponent {
        return DefaultPatientHomeComponent(
            componentContext = ctx,
            patientId = "pat-1",
            getDashboard = GetPatientDashboardUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_dashboard_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertEquals(3, state.upcomingCount)
        assertEquals(1, state.recentDoctors.size)
        assertEquals("doc-1", state.recentDoctors.first().id)
        assertNotNull(state.nextAppointment)
        assertEquals("apt-1", state.nextAppointment?.id)
        assertNull(state.error)
    }

    @Test
    fun load_failure_sets_error_in_state() = runTest {
        val component = createComponent(
            dataSource = FakeHomePatientDataSource(error = Exception("Network error")),
        )

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun onRefresh_reloads_dashboard() = runTest {
        val component = createComponent()
        assertFalse(component.state.value.isLoading)

        component.onRefresh()

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
    }

    @Test
    fun onSearchTapped_emits_NavigateToSearch() = runTest {
        val outputs = mutableListOf<PatientHomeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onSearchTapped()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PatientHomeComponent.Output.NavigateToSearch)
    }

    @Test
    fun onDoctorTapped_emits_NavigateToDoctorProfile_with_correct_id() = runTest {
        val outputs = mutableListOf<PatientHomeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onDoctorTapped("doc-42")

        assertEquals(1, outputs.size)
        val output = outputs.first() as PatientHomeComponent.Output.NavigateToDoctorProfile
        assertEquals("doc-42", output.doctorId)
    }

    @Test
    fun onAssistantChatTapped_emits_NavigateToAssistantChat() = runTest {
        val outputs = mutableListOf<PatientHomeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onAssistantChatTapped()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PatientHomeComponent.Output.NavigateToAssistantChat)
    }

    @Test
    fun onAppointmentsTapped_emits_NavigateToAppointments() = runTest {
        val outputs = mutableListOf<PatientHomeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onAppointmentsTapped()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PatientHomeComponent.Output.NavigateToAppointments)
    }

    @Test
    fun onProfileTapped_emits_NavigateToProfile() = runTest {
        val outputs = mutableListOf<PatientHomeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onProfileTapped()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PatientHomeComponent.Output.NavigateToProfile)
    }

    @Test
    fun onNavigateToHistoryAccess_emits_NavigateToHistoryAccess() = runTest {
        val outputs = mutableListOf<PatientHomeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onNavigateToHistoryAccess()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is PatientHomeComponent.Output.NavigateToHistoryAccess)
    }

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val component = createComponent(
            dataSource = FakeHomePatientDataSource(error = Exception("Fail")),
        )
        assertNotNull(component.state.value.error)

        component.onErrorDismissed()

        assertNull(component.state.value.error)
    }
}
