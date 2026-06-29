package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.application.ResubmitOnboardingUseCase
import com.inclinic.app.features.doctor.onboarding.fakes.FakeDoctorOnboardingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultCorregirSolicitudComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val fakeRepo = FakeDoctorOnboardingRepository()

    private fun resubmitUseCase() = ResubmitOnboardingUseCase(fakeRepo, dispatchers)

    private fun makeComponent(
        initialCorrections: Map<String, String> = emptyMap(),
    ) = DefaultCorregirSolicitudComponent(context, dispatchers, resubmitUseCase(), initialCorrections)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_uses_provided_corrections() {
        val corrections = mapOf("phone" to "999888777")
        val c = makeComponent(initialCorrections = corrections)
        assertEquals(corrections, c.state.value.corrections)
    }

    @Test
    fun initial_state_not_submitting_not_success() {
        val c = makeComponent()
        assertFalse(c.state.value.isSubmitting)
        assertFalse(c.state.value.submitSuccess)
        assertNull(c.state.value.error)
    }

    // ── Field changes ─────────────────────────────────────────────────────────

    @Test
    fun onFieldChanged_adds_or_updates_corrections() {
        val c = makeComponent()
        c.onFieldChanged("cmpLicense", "CMP-99999")
        assertEquals("CMP-99999", c.state.value.corrections["cmpLicense"])
    }

    @Test
    fun onFieldChanged_overwrites_existing_value_for_same_key() {
        val c = makeComponent(initialCorrections = mapOf("phone" to "old"))
        c.onFieldChanged("phone", "new")
        assertEquals("new", c.state.value.corrections["phone"])
    }

    @Test
    fun onFieldChanged_clears_error() {
        val c = makeComponent()
        c.onSubmitClicked() // triggers error (empty corrections)
        assertNotNull(c.state.value.error)
        c.onFieldChanged("phone", "123")
        assertNull(c.state.value.error)
    }

    // ── Submit validation ─────────────────────────────────────────────────────

    @Test
    fun onSubmitClicked_with_empty_corrections_sets_error() = runTest {
        val c = makeComponent()
        c.onSubmitClicked()
        assertNotNull(c.state.value.error)
        assertEquals(0, fakeRepo.resubmitCallCount)
    }

    // ── Submit success ────────────────────────────────────────────────────────

    @Test
    fun onSubmitClicked_with_corrections_calls_resubmit_use_case() = runTest {
        fakeRepo.resubmitResult = Result.success(Unit)
        val c = makeComponent(initialCorrections = mapOf("phone" to "999888777"))
        c.onSubmitClicked()
        assertEquals(1, fakeRepo.resubmitCallCount)
        assertEquals(mapOf("phone" to "999888777"), fakeRepo.lastResubmitCorrections)
    }

    @Test
    fun onSubmitClicked_success_sets_submitSuccess_true() = runTest {
        fakeRepo.resubmitResult = Result.success(Unit)
        val c = makeComponent(initialCorrections = mapOf("field" to "value"))
        c.onSubmitClicked()
        assertTrue(c.state.value.submitSuccess)
        assertFalse(c.state.value.isSubmitting)
    }

    // ── Submit failure ────────────────────────────────────────────────────────

    @Test
    fun onSubmitClicked_failure_sets_error_and_not_success() = runTest {
        fakeRepo.resubmitResult = Result.failure(RuntimeException("Server error"))
        val c = makeComponent(initialCorrections = mapOf("field" to "value"))
        c.onSubmitClicked()
        assertNotNull(c.state.value.error)
        assertFalse(c.state.value.submitSuccess)
        assertFalse(c.state.value.isSubmitting)
    }

    // ── Error dismissed ───────────────────────────────────────────────────────

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val c = makeComponent()
        c.onSubmitClicked()
        assertNotNull(c.state.value.error)
        c.onErrorDismissed()
        assertNull(c.state.value.error)
    }
}
