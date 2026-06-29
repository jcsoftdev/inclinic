@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.no_shows.presentation

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.no_shows.application.GetNoShowQueueUseCase
import com.inclinic.app.features.doctor.no_shows.core.model.PaymentHoldStatus
import com.inclinic.app.features.doctor.no_shows.fakes.FakeNoShowAppointmentDataSource
import com.inclinic.app.features.doctor.no_shows.fakes.stubNoShowItem
import com.inclinic.app.features.doctor.no_shows.presentation.component.DefaultNoShowQueueComponent
import com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowQueueComponent
import com.inclinic.app.features.doctor.no_shows.presentation.component.NoShowTab
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultNoShowQueueComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val dataSource = FakeNoShowAppointmentDataSource()

    private fun createComponent(
        onOutput: (NoShowQueueComponent.Output) -> Unit = {},
    ) = DefaultNoShowQueueComponent(
        componentContext = ctx,
        getNoShowQueue = GetNoShowQueueUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = onOutput,
    )

    // ── Initial load ──────────────────────────────────────────────────────────

    @Test
    fun loads_on_init_and_not_loading_after() = runTest {
        dataSource.noShowResult = Result.success(listOf(stubNoShowItem("n1")))
        val component = createComponent()
        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
    }

    @Test
    fun splits_held_into_pending_and_released_into_resolved() = runTest {
        dataSource.noShowResult = Result.success(listOf(
            stubNoShowItem("n1", PaymentHoldStatus.HELD),
            stubNoShowItem("n2", PaymentHoldStatus.RELEASED),
            stubNoShowItem("n3", PaymentHoldStatus.REFUNDED),
        ))
        val component = createComponent()

        assertEquals(1, component.state.value.pending.size, "Pending should contain HELD items")
        assertEquals(2, component.state.value.resolved.size, "Resolved should contain RELEASED + REFUNDED items")
        assertEquals("n1", component.state.value.pending[0].id)
    }

    @Test
    fun unknown_hold_status_excluded_from_both_tabs() = runTest {
        dataSource.noShowResult = Result.success(listOf(
            stubNoShowItem("n1", PaymentHoldStatus.UNKNOWN),
        ))
        val component = createComponent()

        assertEquals(0, component.state.value.pending.size)
        assertEquals(0, component.state.value.resolved.size)
    }

    @Test
    fun initial_tab_is_pending() = runTest {
        val component = createComponent()
        assertEquals(NoShowTab.Pending, component.state.value.selectedTab)
    }

    // ── Tab selection ─────────────────────────────────────────────────────────

    @Test
    fun on_tab_selected_switches_tab() = runTest {
        val component = createComponent()
        component.onTabSelected(NoShowTab.Resolved)
        assertEquals(NoShowTab.Resolved, component.state.value.selectedTab)
    }

    @Test
    fun tab_switch_does_not_reload_data() = runTest {
        dataSource.noShowResult = Result.success(emptyList())
        val component = createComponent()
        val callsBefore = dataSource.callCount
        component.onTabSelected(NoShowTab.Resolved)
        assertEquals(callsBefore, dataSource.callCount, "Tab switch must not trigger a reload")
    }

    // ── Error handling ────────────────────────────────────────────────────────

    @Test
    fun error_propagates_when_load_fails() = runTest {
        dataSource.noShowResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.pending.isEmpty())
        assertTrue(component.state.value.resolved.isEmpty())
    }

    @Test
    fun on_retry_reloads_data() = runTest {
        dataSource.noShowResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        assertNotNull(component.state.value.error)

        dataSource.noShowResult = Result.success(listOf(stubNoShowItem("n1")))
        component.onRetry()

        assertNull(component.state.value.error)
        assertEquals(1, component.state.value.pending.size)
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @Test
    fun on_back_emits_back_output() {
        var output: NoShowQueueComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onBack()
        assertEquals(NoShowQueueComponent.Output.Back, output)
    }

    // ── Flow emission ─────────────────────────────────────────────────────────

    @Test
    fun state_settles_not_loading_via_flow() = runTest {
        dataSource.noShowResult = Result.success(listOf(stubNoShowItem("n1")))
        val component = createComponent()

        component.state.asFlow().test {
            val settled = expectMostRecentItem()
            assertFalse(settled.isLoading)
            assertNull(settled.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T : Any> com.arkivanov.decompose.value.Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { trySend(it) }
    awaitClose { cancellation.cancel() }
}
