package com.inclinic.app.features.doctor.profile.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.profile.application.EditSpecialtiesUseCase
import com.inclinic.app.features.doctor.profile.application.GetDoctorProfileUseCase
import com.inclinic.app.features.doctor.profile.fakes.FakeDoctorProfileRepository
import com.inclinic.app.features.doctor.profile.presentation.component.DefaultEditSpecialtiesComponent
import com.inclinic.app.features.doctor.profile.presentation.component.EditSpecialtiesComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultEditSpecialtiesComponentTest {

    private val lifecycle = LifecycleRegistry()
    private val fakeRepo = FakeDoctorProfileRepository()
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        onOutput: (EditSpecialtiesComponent.Output) -> Unit = {},
    ): DefaultEditSpecialtiesComponent {
        lifecycle.resume()
        val ctx = DefaultComponentContext(lifecycle)
        return DefaultEditSpecialtiesComponent(
            componentContext = ctx,
            getProfile = GetDoctorProfileUseCase(fakeRepo, dispatchers),
            editSpecialties = EditSpecialtiesUseCase(fakeRepo, dispatchers),
            dispatchers = dispatchers,
            onOutput = onOutput,
        )
    }

    @Test
    fun initial_load_populates_specialties_from_profile() = runTest {
        val component = makeComponent()

        val s = component.state.value
        assertEquals(1, s.availableSpecialties.size)
        assertEquals("sp-1", s.availableSpecialties[0].id)
        assertTrue("sp-1" in s.selectedIds)
    }

    @Test
    fun onToggleSpecialty_removes_when_selected() = runTest {
        val component = makeComponent()
        assertTrue("sp-1" in component.state.value.selectedIds)

        component.onToggleSpecialty("sp-1")

        assertFalse("sp-1" in component.state.value.selectedIds)
    }

    @Test
    fun onToggleSpecialty_adds_when_not_selected() = runTest {
        val component = makeComponent()
        component.onToggleSpecialty("sp-1") // deselect
        assertFalse("sp-1" in component.state.value.selectedIds)

        component.onToggleSpecialty("sp-1") // re-select

        assertTrue("sp-1" in component.state.value.selectedIds)
    }

    @Test
    fun onSave_calls_editSpecialties_with_current_selected_ids() = runTest {
        val component = makeComponent()

        component.onSave()

        assertEquals(1, fakeRepo.editSpecialtiesCallCount)
        assertNotNull(fakeRepo.lastEditedSpecialtyIds)
        assertTrue(fakeRepo.lastEditedSpecialtyIds!!.contains("sp-1"))
    }

    @Test
    fun onSave_emits_Saved_output_on_success() = runTest {
        var output: EditSpecialtiesComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onSave()

        assertTrue(output is EditSpecialtiesComponent.Output.Saved)
    }

    @Test
    fun onSave_sets_error_on_failure() = runTest {
        fakeRepo.editSpecialtiesResult = Result.failure(RuntimeException("Forbidden"))
        val component = makeComponent()

        component.onSave()

        assertNotNull(component.state.value.error)
        assertFalse(component.state.value.saveSuccess)
    }

    @Test
    fun onBack_emits_Back_output() = runTest {
        var output: EditSpecialtiesComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onBack()

        assertTrue(output is EditSpecialtiesComponent.Output.Back)
    }

    @Test
    fun onNavigateToRequestSpecialty_emits_NavigateToRequestSpecialty_output() = runTest {
        var output: EditSpecialtiesComponent.Output? = null
        val component = makeComponent(onOutput = { output = it })

        component.onNavigateToRequestSpecialty()

        assertTrue(output is EditSpecialtiesComponent.Output.NavigateToRequestSpecialty)
    }
}
