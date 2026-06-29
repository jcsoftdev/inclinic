@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.DoctorPlan
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.core.model.Review
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import com.inclinic.app.features.patient.infrastructure.remote.DoctorFilters
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PagedDoctors
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private fun consultTypeTestDoctor(id: String = "doc-1"): Doctor = Doctor(
    id = id, fullName = "Dr. Lucia Paredes", email = "lucia@test.com", photoUrl = null,
    specialties = emptyList(), plan = DoctorPlan.PREMIUM, ratingAverage = 4.9, ratingsCount = 50,
    consultationFee = 200.0, homeVisitAvailable = true, virtualVisitAvailable = true,
    bio = "Neuróloga", isVerified = true, cmpLicense = "CMP-789",
    onboardingStatus = OnboardingStatus.APPROVED,
)

private class FakeConsultTypeDoctorSearchDataSource(
    private val doctorResult: Result<Doctor> = Result.success(consultTypeTestDoctor()),
) : DoctorSearchDataSource {
    override suspend fun searchDoctors(filters: DoctorFilters, page: Int): Result<PagedDoctors> =
        Result.success(PagedDoctors(emptyList(), false))

    override suspend fun getDoctorById(doctorId: String): Result<Doctor> = doctorResult

    override suspend fun getDoctorReviews(doctorId: String, page: Int): Result<List<Review>> =
        Result.success(emptyList())
}

class DefaultConsultTypeComponentTest {

    private val dispatchers = TestAppDispatchers()
    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle = lifecycle)

    private fun createComponent(
        doctorId: String = "doc-1",
        dataSource: FakeConsultTypeDoctorSearchDataSource = FakeConsultTypeDoctorSearchDataSource(),
        outputs: MutableList<ConsultTypeComponent.Output> = mutableListOf(),
    ): DefaultConsultTypeComponent {
        return DefaultConsultTypeComponent(
            componentContext = ctx,
            doctorId = doctorId,
            getDoctorDetail = GetDoctorDetailUseCase(dataSource, dispatchers),
            dispatchers = dispatchers,
            onOutput = outputs::add,
        )
    }

    @Test
    fun load_success_sets_doctor_in_state() = runTest {
        val component = createComponent()

        val state = component.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.doctor)
        assertEquals("doc-1", state.doctor?.id)
        assertEquals("Dr. Lucia Paredes", state.doctor?.fullName)
    }

    @Test
    fun initial_selectedType_is_PRESENCIAL() = runTest {
        val component = createComponent()

        assertEquals(ConsultType.PRESENCIAL, component.state.value.selectedType)
    }

    @Test
    fun onTypeSelected_updates_to_TELEMEDICINE() = runTest {
        val component = createComponent()

        component.onTypeSelected(ConsultType.TELEMEDICINE)

        assertEquals(ConsultType.TELEMEDICINE, component.state.value.selectedType)
    }

    @Test
    fun onTypeSelected_updates_to_HOME_VISIT() = runTest {
        val component = createComponent()

        component.onTypeSelected(ConsultType.HOME_VISIT)

        assertEquals(ConsultType.HOME_VISIT, component.state.value.selectedType)
    }

    @Test
    fun onContinue_with_PRESENCIAL_emits_NavigateToAvailability_with_office() = runTest {
        val outputs = mutableListOf<ConsultTypeComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onTypeSelected(ConsultType.PRESENCIAL)

        component.onContinue()

        assertEquals(1, outputs.size)
        val output = outputs.first() as ConsultTypeComponent.Output.NavigateToAvailability
        assertEquals("doc-1", output.doctorId)
        assertEquals("office", output.consultType)
    }

    @Test
    fun onContinue_with_TELEMEDICINE_emits_telemedicine_consultType() = runTest {
        val outputs = mutableListOf<ConsultTypeComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onTypeSelected(ConsultType.TELEMEDICINE)

        component.onContinue()

        assertEquals(1, outputs.size)
        val output = outputs.first() as ConsultTypeComponent.Output.NavigateToAvailability
        assertEquals("telemedicine", output.consultType)
    }

    @Test
    fun onContinue_with_HOME_VISIT_emits_home_consultType() = runTest {
        val outputs = mutableListOf<ConsultTypeComponent.Output>()
        val component = createComponent(outputs = outputs)
        component.onTypeSelected(ConsultType.HOME_VISIT)

        component.onContinue()

        assertEquals(1, outputs.size)
        val output = outputs.first() as ConsultTypeComponent.Output.NavigateToAvailability
        assertEquals("home", output.consultType)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        val outputs = mutableListOf<ConsultTypeComponent.Output>()
        val component = createComponent(outputs = outputs)

        component.onBack()

        assertEquals(1, outputs.size)
        assertTrue(outputs.first() is ConsultTypeComponent.Output.Back)
    }
}
