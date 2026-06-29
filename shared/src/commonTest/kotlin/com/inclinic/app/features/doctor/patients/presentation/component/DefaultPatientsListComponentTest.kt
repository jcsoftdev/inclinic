package com.inclinic.app.features.doctor.patients.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.patients.application.GetDoctorPatientsUseCase
import com.inclinic.app.features.doctor.patients.core.model.PatientList
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.core.model.PatientListStats
import com.inclinic.app.features.doctor.patients.core.model.PatientStatus
import com.inclinic.app.features.doctor.patients.fakes.FakeDoctorPatientsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultPatientsListComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorPatientsRepository()
    private var capturedOutput: PatientsListComponent.Output? = null

    private fun createComponent(): DefaultPatientsListComponent {
        capturedOutput = null
        return DefaultPatientsListComponent(
            componentContext = componentContext,
            getPatients = GetDoctorPatientsUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = { capturedOutput = it },
        )
    }

    // ── Initial load ──────────────────────────────────────────────────────────

    @Test
    fun initial_state_loads_patients_and_stats_on_success() = runTest {
        val patients = listOf(PatientListItem("p1", "Ana", null, null, 1, PatientStatus.ACTIVE))
        fakeRepo.getPatientsResult = Result.success(
            PatientList(items = patients, stats = PatientListStats(total = 1, active = 1, premium = 0)),
        )

        val component = createComponent()

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertEquals(patients, component.state.value.patients)
        assertEquals(1, component.state.value.stats.total)
    }

    @Test
    fun initial_state_sets_error_on_failure() = runTest {
        fakeRepo.getPatientsResult = Result.failure(RuntimeException("Boom"))

        val component = createComponent()

        assertFalse(component.state.value.isLoading)
        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.patients.isEmpty())
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    @Test
    fun onFilterChange_premium_filters_visible_patients() = runTest {
        fakeRepo.getPatientsResult = Result.success(
            PatientList(
                items = listOf(
                    PatientListItem("p1", "Premium One", null, null, 1, PatientStatus.PREMIUM),
                    PatientListItem("p2", "Active Two", null, null, 1, PatientStatus.ACTIVE),
                    PatientListItem("p3", "Inactive Three", null, null, 0, PatientStatus.INACTIVE),
                ),
            ),
        )
        val component = createComponent()

        component.onFilterChange(PatientsFilter.PREMIUM)

        assertEquals(PatientsFilter.PREMIUM, component.state.value.filter)
        assertEquals(listOf("p1"), component.state.value.visiblePatients.map { it.id })
    }

    @Test
    fun onFilterChange_active_includes_premium_and_active() = runTest {
        fakeRepo.getPatientsResult = Result.success(
            PatientList(
                items = listOf(
                    PatientListItem("p1", "Premium One", null, null, 1, PatientStatus.PREMIUM),
                    PatientListItem("p2", "Active Two", null, null, 1, PatientStatus.ACTIVE),
                    PatientListItem("p3", "Inactive Three", null, null, 0, PatientStatus.INACTIVE),
                ),
            ),
        )
        val component = createComponent()

        component.onFilterChange(PatientsFilter.ACTIVE)

        assertEquals(listOf("p1", "p2"), component.state.value.visiblePatients.map { it.id })
    }

    @Test
    fun onFilterChange_free_shows_only_active_non_premium_patients() = runTest {
        fakeRepo.getPatientsResult = Result.success(
            PatientList(
                items = listOf(
                    PatientListItem("p1", "Premium One", null, null, 1, PatientStatus.PREMIUM),
                    PatientListItem("p2", "Active Free", null, null, 1, PatientStatus.ACTIVE),
                    PatientListItem("p3", "Inactive Three", null, null, 0, PatientStatus.INACTIVE),
                ),
            ),
        )
        val component = createComponent()

        component.onFilterChange(PatientsFilter.FREE)

        assertEquals(PatientsFilter.FREE, component.state.value.filter)
        assertEquals(listOf("p2"), component.state.value.visiblePatients.map { it.id })
    }

    // ── Navigation outputs ────────────────────────────────────────────────────

    @Test
    fun onPatientClicked_emits_NavigateToPatient_output() {
        fakeRepo.getPatientsResult = Result.success(PatientList())
        val component = createComponent()

        component.onPatientClicked("p42")

        assertEquals(PatientsListComponent.Output.NavigateToPatient("p42"), capturedOutput)
    }

    @Test
    fun onSearchClicked_emits_NavigateToSearch_output() {
        fakeRepo.getPatientsResult = Result.success(PatientList())
        val component = createComponent()

        component.onSearchClicked()

        assertEquals(PatientsListComponent.Output.NavigateToSearch, capturedOutput)
    }

    @Test
    fun onBack_emits_Back_output() {
        fakeRepo.getPatientsResult = Result.success(PatientList())
        val component = createComponent()

        component.onBack()

        assertEquals(PatientsListComponent.Output.Back, capturedOutput)
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Test
    fun onRefresh_reloads_patients() = runTest {
        fakeRepo.getPatientsResult = Result.success(PatientList())
        val component = createComponent()
        val newPatients = listOf(PatientListItem("p1", "Reloaded", null, null, 0))
        fakeRepo.getPatientsResult = Result.success(PatientList(items = newPatients))

        component.onRefresh()

        assertEquals(newPatients, component.state.value.patients)
        assertEquals(2, fakeRepo.getPatientsCallCount) // init + refresh
    }
}
