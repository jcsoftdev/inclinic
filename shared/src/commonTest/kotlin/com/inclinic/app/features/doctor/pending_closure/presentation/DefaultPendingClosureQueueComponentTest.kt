@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.inclinic.app.features.doctor.pending_closure.presentation

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.pending_closure.application.GetPendingClosureQueueUseCase
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
import com.inclinic.app.features.doctor.pending_closure.fakes.FakePendingClosureAppointmentDataSource
import com.inclinic.app.features.doctor.pending_closure.presentation.component.DefaultPendingClosureQueueComponent
import com.inclinic.app.features.doctor.pending_closure.presentation.component.PendingClosureQueueComponent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun stubPendingClosureItem(id: String) = PendingClosureItem(
    id = id,
    patientName = "María García",
    startTime = "2026-06-01T09:00:00.000Z",
    price = 70.0,
    specialtyName = "Cardiología",
    visitType = "CLINIC",
)

class DefaultPendingClosureQueueComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val dataSource = FakePendingClosureAppointmentDataSource()

    private fun createComponent(
        onOutput: (PendingClosureQueueComponent.Output) -> Unit = {},
    ) = DefaultPendingClosureQueueComponent(
        componentContext = ctx,
        getPendingClosureQueue = GetPendingClosureQueueUseCase(dataSource, dispatchers),
        dispatchers = dispatchers,
        onOutput = onOutput,
    )

    @Test
    fun loads_on_init_and_not_loading_after() = runTest {
        dataSource.pendingClosureResult = Result.success(listOf(stubPendingClosureItem("a1")))
        val component = createComponent()
        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertEquals(1, component.state.value.items.size)
    }

    @Test
    fun error_propagates_when_load_fails() = runTest {
        dataSource.pendingClosureResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        assertNotNull(component.state.value.error)
        assertTrue(component.state.value.items.isEmpty())
    }

    @Test
    fun on_retry_reloads_data() = runTest {
        dataSource.pendingClosureResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        assertNotNull(component.state.value.error)

        dataSource.pendingClosureResult = Result.success(listOf(stubPendingClosureItem("a1")))
        component.onRetry()

        assertNull(component.state.value.error)
        assertEquals(1, component.state.value.items.size)
    }

    @Test
    fun on_appointment_tapped_emits_navigate_to_detail() {
        var output: PendingClosureQueueComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onAppointmentTapped("a1")
        assertEquals(PendingClosureQueueComponent.Output.NavigateToDetail("a1"), output)
    }

    @Test
    fun on_back_emits_back_output() {
        var output: PendingClosureQueueComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onBack()
        assertEquals(PendingClosureQueueComponent.Output.Back, output)
    }
}
