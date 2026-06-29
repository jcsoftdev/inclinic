package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.PersonalData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultStepDatosComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        onContinue: (PersonalData) -> Unit = {},
    ) = DefaultStepDatosComponent(context, dispatchers, onContinue)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_has_empty_fields_no_errors() {
        val c = makeComponent()
        val s = c.state.value
        assertEquals("", s.firstName)
        assertEquals("", s.lastName)
        assertEquals("", s.cmpLicense)
        assertEquals("", s.phone)
        assertNull(s.firstNameError)
        assertNull(s.lastNameError)
        assertNull(s.cmpLicenseError)
        assertNull(s.phoneError)
    }

    // ── Field updates ─────────────────────────────────────────────────────────

    @Test
    fun onFirstNameChanged_updates_state() {
        val c = makeComponent()
        c.onFirstNameChanged("Juan")
        assertEquals("Juan", c.state.value.firstName)
    }

    @Test
    fun onLastNameChanged_updates_state() {
        val c = makeComponent()
        c.onLastNameChanged("Perez")
        assertEquals("Perez", c.state.value.lastName)
    }

    @Test
    fun onCmpLicenseChanged_updates_state() {
        val c = makeComponent()
        c.onCmpLicenseChanged("CMP-12345")
        assertEquals("CMP-12345", c.state.value.cmpLicense)
    }

    @Test
    fun onPhoneChanged_updates_state() {
        val c = makeComponent()
        c.onPhoneChanged("987654321")
        assertEquals("987654321", c.state.value.phone)
    }

    @Test
    fun field_change_clears_existing_field_error() {
        val c = makeComponent()
        c.onContinueClicked() // trigger validation errors
        assertNotNull(c.state.value.firstNameError)
        c.onFirstNameChanged("Juan")
        assertNull(c.state.value.firstNameError)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun onContinueClicked_with_empty_fields_sets_all_field_errors() = runTest {
        val c = makeComponent()
        c.onContinueClicked()
        val s = c.state.value
        assertNotNull(s.firstNameError)
        assertNotNull(s.lastNameError)
        assertNotNull(s.cmpLicenseError)
        assertNotNull(s.phoneError)
    }

    @Test
    fun onContinueClicked_with_only_firstName_missing_sets_firstName_error_only() = runTest {
        val c = makeComponent()
        c.onLastNameChanged("Perez")
        c.onCmpLicenseChanged("CMP-12345")
        c.onPhoneChanged("987654321")
        c.onContinueClicked()
        assertNotNull(c.state.value.firstNameError)
        assertNull(c.state.value.lastNameError)
        assertNull(c.state.value.cmpLicenseError)
        assertNull(c.state.value.phoneError)
    }

    // ── Success / continue ────────────────────────────────────────────────────

    @Test
    fun onContinueClicked_with_valid_fields_invokes_callback() = runTest {
        var received: PersonalData? = null
        val c = makeComponent(onContinue = { received = it })
        c.onFirstNameChanged("Juan")
        c.onLastNameChanged("Perez")
        c.onCmpLicenseChanged("CMP-12345")
        c.onPhoneChanged("987654321")
        c.onContinueClicked()
        assertNotNull(received)
        assertEquals("Juan", received!!.firstName)
        assertEquals("Perez", received!!.lastName)
        assertEquals("CMP-12345", received!!.cmpLicense)
        assertEquals("987654321", received!!.phone)
    }

    @Test
    fun onContinueClicked_valid_does_not_set_errors() = runTest {
        val c = makeComponent()
        c.onFirstNameChanged("Juan")
        c.onLastNameChanged("Perez")
        c.onCmpLicenseChanged("CMP-12345")
        c.onPhoneChanged("987654321")
        c.onContinueClicked()
        val s = c.state.value
        assertNull(s.firstNameError)
        assertNull(s.lastNameError)
        assertNull(s.cmpLicenseError)
        assertNull(s.phoneError)
    }
}
