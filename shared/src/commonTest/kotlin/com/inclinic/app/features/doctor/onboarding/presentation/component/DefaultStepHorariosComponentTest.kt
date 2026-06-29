package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.WeeklySchedule
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultStepHorariosComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val context = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()

    private fun makeComponent(
        onContinue: (WeeklySchedule) -> Unit = {},
    ) = DefaultStepHorariosComponent(context, dispatchers, onContinue)

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    fun initial_state_no_days_enabled_and_no_slots() {
        val c = makeComponent()
        val s = c.state.value
        assertTrue(s.enabledDays.values.none { it })
        assertTrue(s.slots.isEmpty())
        assertFalse(s.canContinue)
    }

    // ── Toggle day ────────────────────────────────────────────────────────────

    @Test
    fun onToggleDay_enables_disabled_day() {
        val c = makeComponent()
        c.onToggleDay("MONDAY")
        assertTrue(c.state.value.enabledDays["MONDAY"] == true)
    }

    @Test
    fun onToggleDay_disables_enabled_day() {
        val c = makeComponent()
        c.onToggleDay("MONDAY")
        c.onToggleDay("MONDAY")
        assertFalse(c.state.value.enabledDays["MONDAY"] == true)
    }

    // ── Toggle slot ───────────────────────────────────────────────────────────

    @Test
    fun onToggleSlot_adds_hour_to_day() {
        val c = makeComponent()
        c.onToggleDay("MONDAY")
        c.onToggleSlot("MONDAY", 9)
        assertTrue(9 in (c.state.value.slots["MONDAY"] ?: emptyList()))
    }

    @Test
    fun onToggleSlot_removes_existing_hour() {
        val c = makeComponent()
        c.onToggleDay("MONDAY")
        c.onToggleSlot("MONDAY", 9)
        c.onToggleSlot("MONDAY", 9)
        assertFalse(9 in (c.state.value.slots["MONDAY"] ?: emptyList()))
    }

    // ── canContinue ───────────────────────────────────────────────────────────

    @Test
    fun canContinue_requires_enabled_day_with_at_least_one_slot() {
        val c = makeComponent()
        c.onToggleDay("FRIDAY")
        assertFalse(c.state.value.canContinue) // day enabled but no slots
        c.onToggleSlot("FRIDAY", 10)
        assertTrue(c.state.value.canContinue)
    }

    // ── Continue ──────────────────────────────────────────────────────────────

    @Test
    fun onContinueClicked_without_active_days_sets_error() = runTest {
        val c = makeComponent()
        c.onContinueClicked()
        assertNotNull(c.state.value.error)
    }

    @Test
    fun onContinueClicked_with_valid_schedule_invokes_callback() = runTest {
        var received: WeeklySchedule? = null
        val c = makeComponent(onContinue = { received = it })
        c.onToggleDay("WEDNESDAY")
        c.onToggleSlot("WEDNESDAY", 8)
        c.onToggleSlot("WEDNESDAY", 10)
        c.onContinueClicked()
        assertNotNull(received)
        assertTrue("WEDNESDAY" in received!!.slots)
        assertEquals(listOf(8, 10), received!!.slots["WEDNESDAY"])
    }

    @Test
    fun onContinueClicked_only_includes_active_days_in_schedule() = runTest {
        var received: WeeklySchedule? = null
        val c = makeComponent(onContinue = { received = it })
        c.onToggleDay("MONDAY")
        c.onToggleSlot("MONDAY", 9)
        c.onToggleDay("TUESDAY") // enabled but no slots — should be excluded
        c.onContinueClicked()
        assertNotNull(received)
        assertTrue("MONDAY" in received!!.slots)
        assertFalse("TUESDAY" in received!!.slots)
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
