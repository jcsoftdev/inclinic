package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.PriceConfig
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultStepPreciosComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        onContinue: (PriceConfig) -> Unit = {},
    ) = DefaultStepPreciosComponent(context, dispatchers, onContinue)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_empty_fee_presential_enabled() {
        val c = makeComponent()
        val s = c.state.value
        assertEquals("", s.consultationFeeText)
        assertTrue(s.supportsPresential)
        assertFalse(s.supportsVirtual)
        assertNull(s.consultationFeeError)
    }

    // ── Field updates ─────────────────────────────────────────────────────────

    @Test
    fun onConsultationFeeChanged_updates_state() {
        val c = makeComponent()
        c.onConsultationFeeChanged("150.00")
        assertEquals("150.00", c.state.value.consultationFeeText)
    }

    @Test
    fun onConsultationFeeChanged_clears_fee_error() {
        val c = makeComponent()
        c.onContinueClicked() // trigger fee error
        assertNotNull(c.state.value.consultationFeeError)
        c.onConsultationFeeChanged("100")
        assertNull(c.state.value.consultationFeeError)
    }

    @Test
    fun onTogglePresential_updates_state() {
        val c = makeComponent()
        c.onTogglePresential(false)
        assertFalse(c.state.value.supportsPresential)
    }

    @Test
    fun onToggleVirtual_updates_state() {
        val c = makeComponent()
        c.onToggleVirtual(true)
        assertTrue(c.state.value.supportsVirtual)
    }

    // ── Validation ────────────────────────────────────────────────────────────

    @Test
    fun onContinueClicked_with_empty_fee_sets_fee_error() = runTest {
        val c = makeComponent()
        c.onContinueClicked()
        assertNotNull(c.state.value.consultationFeeError)
    }

    @Test
    fun onContinueClicked_with_zero_fee_sets_fee_error() = runTest {
        val c = makeComponent()
        c.onConsultationFeeChanged("0")
        c.onContinueClicked()
        assertNotNull(c.state.value.consultationFeeError)
    }

    @Test
    fun onContinueClicked_with_no_modality_sets_error() = runTest {
        val c = makeComponent()
        c.onConsultationFeeChanged("150")
        c.onTogglePresential(false)
        c.onToggleVirtual(false)
        c.onContinueClicked()
        assertNotNull(c.state.value.error)
    }

    @Test
    fun canContinue_false_when_fee_empty() {
        val c = makeComponent()
        assertFalse(c.state.value.canContinue)
    }

    @Test
    fun canContinue_true_with_valid_fee_and_modality() {
        val c = makeComponent()
        c.onConsultationFeeChanged("200")
        assertTrue(c.state.value.canContinue)
    }

    // ── Success ───────────────────────────────────────────────────────────────

    @Test
    fun onContinueClicked_valid_invokes_callback_with_correct_data() = runTest {
        var received: PriceConfig? = null
        val c = makeComponent(onContinue = { received = it })
        c.onConsultationFeeChanged("250.50")
        c.onToggleVirtual(true)
        c.onContinueClicked()
        assertNotNull(received)
        assertEquals(250.50, received!!.consultationFee)
        assertTrue(received!!.supportsPresential)
        assertTrue(received!!.supportsVirtual)
    }

    // ── Error dismissed ───────────────────────────────────────────────────────

    @Test
    fun onErrorDismissed_clears_error() = runTest {
        val c = makeComponent()
        c.onConsultationFeeChanged("150")
        c.onTogglePresential(false)
        c.onToggleVirtual(false)
        c.onContinueClicked()
        assertNotNull(c.state.value.error)
        c.onErrorDismissed()
        assertNull(c.state.value.error)
    }
}
