package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultStepEspecialidadesComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val specialties = listOf("cardiology", "dermatology", "pediatrics")

    private fun makeComponent(
        onContinue: (List<String>) -> Unit = {},
    ) = DefaultStepEspecialidadesComponent(context, dispatchers, specialties, onContinue)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_has_available_specialties_and_no_selection() {
        val c = makeComponent()
        assertEquals(specialties, c.state.value.availableSpecialties)
        assertTrue(c.state.value.selectedSpecialtyIds.isEmpty())
        assertFalse(c.state.value.canContinue)
    }

    // ── Toggle specialty ──────────────────────────────────────────────────────

    @Test
    fun onToggleSpecialty_selects_when_not_selected() {
        val c = makeComponent()
        c.onToggleSpecialty("cardiology")
        assertTrue("cardiology" in c.state.value.selectedSpecialtyIds)
    }

    @Test
    fun onToggleSpecialty_deselects_when_already_selected() {
        val c = makeComponent()
        c.onToggleSpecialty("cardiology")
        c.onToggleSpecialty("cardiology")
        assertFalse("cardiology" in c.state.value.selectedSpecialtyIds)
    }

    @Test
    fun toggle_multiple_specialties_accumulates_selection() {
        val c = makeComponent()
        c.onToggleSpecialty("cardiology")
        c.onToggleSpecialty("dermatology")
        val selected = c.state.value.selectedSpecialtyIds
        assertTrue("cardiology" in selected)
        assertTrue("dermatology" in selected)
        assertEquals(2, selected.size)
    }

    @Test
    fun canContinue_is_true_when_at_least_one_selected() {
        val c = makeComponent()
        assertFalse(c.state.value.canContinue)
        c.onToggleSpecialty("cardiology")
        assertTrue(c.state.value.canContinue)
    }

    // ── Continue ──────────────────────────────────────────────────────────────

    @Test
    fun onContinueClicked_without_selection_sets_error() = runTest {
        val c = makeComponent()
        c.onContinueClicked()
        assertNotNull(c.state.value.error)
    }

    @Test
    fun onContinueClicked_with_selection_invokes_callback() = runTest {
        var received: List<String>? = null
        val c = makeComponent(onContinue = { received = it })
        c.onToggleSpecialty("cardiology")
        c.onToggleSpecialty("pediatrics")
        c.onContinueClicked()
        assertNotNull(received)
        assertTrue("cardiology" in received!!)
        assertTrue("pediatrics" in received!!)
    }

    @Test
    fun onContinueClicked_valid_clears_error() = runTest {
        val c = makeComponent()
        c.onContinueClicked() // trigger error
        assertNotNull(c.state.value.error)
        c.onToggleSpecialty("cardiology")
        c.onContinueClicked()
        assertNull(c.state.value.error)
    }

    // ── Error dismissed ───────────────────────────────────────────────────────

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val c = makeComponent()
        c.onContinueClicked()
        assertNotNull(c.state.value.error)
        c.onErrorDismissed()
        assertNull(c.state.value.error)
    }
}
