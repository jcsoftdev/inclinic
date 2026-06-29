package com.inclinic.app.features.doctor.patients.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.patients.application.SearchPatientByEmailUseCase
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.fakes.FakeDoctorPatientsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultSearchPatientComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val componentContext = DefaultComponentContext(lifecycle = lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorPatientsRepository()
    private var capturedOutput: SearchPatientComponent.Output? = null

    private fun createComponent(): DefaultSearchPatientComponent {
        capturedOutput = null
        return DefaultSearchPatientComponent(
            componentContext = componentContext,
            searchPatient = SearchPatientByEmailUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = { capturedOutput = it },
        )
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_has_empty_query_and_no_results() {
        val component = createComponent()
        val state = component.state.value
        assertEquals("", state.query)
        assertTrue(state.results.isEmpty())
        assertFalse(state.isSearching)
        assertNull(state.error)
        assertFalse(state.hasSearched)
    }

    // ── Query change ──────────────────────────────────────────────────────────

    @Test
    fun onQueryChange_updates_query_field() {
        val component = createComponent()

        component.onQueryChange("ana@test.com")

        assertEquals("ana@test.com", component.state.value.query)
    }

    @Test
    fun onQueryChange_clears_error() = runTest {
        fakeRepo.searchResult = Result.failure(RuntimeException("Fail"))
        val component = createComponent()
        component.onQueryChange("x")
        component.onSearch()
        assertNotNull(component.state.value.error)

        component.onQueryChange("new@query.com")

        assertNull(component.state.value.error)
    }

    // ── Search ────────────────────────────────────────────────────────────────

    @Test
    fun onSearch_with_empty_query_does_nothing() = runTest {
        val component = createComponent()

        component.onSearch()

        assertEquals(0, fakeRepo.searchCallCount)
        assertFalse(component.state.value.hasSearched)
    }

    @Test
    fun onSearch_with_valid_query_returns_results() = runTest {
        val patients = listOf(PatientListItem("p1", "Ana Garcia", null, null, 0))
        fakeRepo.searchResult = Result.success(patients)
        val component = createComponent()
        component.onQueryChange("ana@test.com")

        component.onSearch()

        assertFalse(component.state.value.isSearching)
        assertTrue(component.state.value.hasSearched)
        assertEquals(patients, component.state.value.results)
        assertNull(component.state.value.error)
    }

    @Test
    fun onSearch_failure_sets_error_and_hasSearched() = runTest {
        fakeRepo.searchResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        component.onQueryChange("test@test.com")

        component.onSearch()

        assertFalse(component.state.value.isSearching)
        assertTrue(component.state.value.hasSearched)
        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.results.isEmpty())
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Test
    fun onPatientClicked_emits_NavigateToPatient_output() {
        val component = createComponent()

        component.onPatientClicked("p99")

        assertEquals(SearchPatientComponent.Output.NavigateToPatient("p99"), capturedOutput)
    }

    @Test
    fun onBack_emits_Back_output() {
        val component = createComponent()

        component.onBack()

        assertEquals(SearchPatientComponent.Output.Back, capturedOutput)
    }
}
