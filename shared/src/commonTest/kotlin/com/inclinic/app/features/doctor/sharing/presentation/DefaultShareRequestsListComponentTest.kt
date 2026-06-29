package com.inclinic.app.features.doctor.sharing.presentation

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequestStatus
import com.inclinic.app.features.doctor.sharing.application.GetIncomingShareRequestsUseCase
import com.inclinic.app.features.doctor.sharing.application.GetOutgoingShareRequestsUseCase
import com.inclinic.app.features.doctor.sharing.application.RespondShareRequestUseCase
import com.inclinic.app.features.doctor.sharing.fakes.FakeDoctorSharingRepository
import com.inclinic.app.features.doctor.sharing.fakes.stubRequest
import com.inclinic.app.features.doctor.sharing.presentation.component.DefaultShareRequestsListComponent
import com.inclinic.app.features.doctor.sharing.presentation.component.ShareRequestsListComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultShareRequestsListComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val repo = FakeDoctorSharingRepository()

    private fun createComponent(
        onOutput: (ShareRequestsListComponent.Output) -> Unit = {},
    ) = DefaultShareRequestsListComponent(
        componentContext = ctx,
        getIncoming = GetIncomingShareRequestsUseCase(repo, dispatchers),
        getOutgoing = GetOutgoingShareRequestsUseCase(repo, dispatchers),
        cancelRequest = RespondShareRequestUseCase(repo, dispatchers),
        dispatchers = dispatchers,
        onOutput = onOutput,
    )

    @Test
    fun initial_state_shows_incoming_tab() {
        val component = createComponent()
        assertTrue(component.state.value.showIncoming)
    }

    @Test
    fun loads_incoming_approved_and_outgoing_pending_on_init() = runTest {
        repo.listResult = Result.success(listOf(
            stubRequest("r1", ShareRequestStatus.APPROVED),
            stubRequest("o1", ShareRequestStatus.PENDING),
        ))
        val component = createComponent()
        assertEquals(1, component.state.value.incomingRequests.size)
        assertEquals("r1", component.state.value.incomingRequests[0].id)
        assertEquals(1, component.state.value.outgoingRequests.size)
        assertEquals("o1", component.state.value.outgoingRequests[0].id)
    }

    @Test
    fun on_select_outgoing_switches_tab() {
        val component = createComponent()
        component.onSelectOutgoing()
        assertFalse(component.state.value.showIncoming)
    }

    @Test
    fun on_select_incoming_switches_tab_back() {
        val component = createComponent()
        component.onSelectOutgoing()
        component.onSelectIncoming()
        assertTrue(component.state.value.showIncoming)
    }

    @Test
    fun on_cancel_calls_cancel_request_on_repo() = runTest {
        repo.listResult = Result.success(listOf(stubRequest("r1", ShareRequestStatus.PENDING)))
        repo.cancelResult = Result.success(Unit)
        val component = createComponent()
        component.onCancel("r1")
        assertEquals("r1", repo.lastCancelledId)
    }

    @Test
    fun on_request_new_emits_navigate_to_request_share_output() {
        var output: ShareRequestsListComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onRequestNew()
        assertEquals(ShareRequestsListComponent.Output.NavigateToRequestShare, output)
    }

    @Test
    fun on_back_emits_back_output() {
        var output: ShareRequestsListComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onBack()
        assertEquals(ShareRequestsListComponent.Output.Back, output)
    }

    @Test
    fun error_propagates_when_list_fails() = runTest {
        repo.listResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        assertEquals("Network error", component.state.value.error)
    }

    @Test
    fun state_clears_error_on_reload() = runTest {
        repo.listResult = Result.success(emptyList())
        val component = createComponent()

        component.state.asFlow().test {
            val loaded = expectMostRecentItem()
            assertFalse(loaded.isLoading)
            assertNull(loaded.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T : Any> com.arkivanov.decompose.value.Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { trySend(it) }
    awaitClose { cancellation.cancel() }
}
