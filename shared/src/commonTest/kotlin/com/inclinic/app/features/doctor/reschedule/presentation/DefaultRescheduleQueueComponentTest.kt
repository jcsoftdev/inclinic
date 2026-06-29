package com.inclinic.app.features.doctor.reschedule.presentation

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.reschedule.application.GetRescheduleRequestsUseCase
import com.inclinic.app.features.doctor.reschedule.application.RespondRescheduleRequestUseCase
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequestStatus
import com.inclinic.app.features.doctor.reschedule.fakes.FakeRescheduleQueueRepository
import com.inclinic.app.features.doctor.reschedule.fakes.stubRequest
import com.inclinic.app.features.doctor.reschedule.presentation.component.DefaultRescheduleQueueComponent
import com.inclinic.app.features.doctor.reschedule.presentation.component.RescheduleQueueComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultRescheduleQueueComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val repo = FakeRescheduleQueueRepository()

    private fun createComponent(
        onOutput: (RescheduleQueueComponent.Output) -> Unit = {},
    ) = DefaultRescheduleQueueComponent(
        componentContext = ctx,
        getRequests = GetRescheduleRequestsUseCase(repo, dispatchers),
        respondReschedule = RespondRescheduleRequestUseCase(repo, dispatchers),
        dispatchers = dispatchers,
        onOutput = onOutput,
    )

    @Test
    fun loads_requests_on_init() = runTest {
        repo.listResult = Result.success(listOf(stubRequest("r1"), stubRequest("r2")))
        val component = createComponent()
        assertEquals(2, component.state.value.requests.size)
        assertFalse(component.state.value.isLoading)
    }

    @Test
    fun on_approve_updates_request_status() = runTest {
        repo.listResult = Result.success(listOf(stubRequest("r1")))
        repo.respondResult = Result.success(stubRequest("r1", RescheduleRequestStatus.APPROVED))
        val component = createComponent()

        component.onApprove("r1")

        assertEquals(RescheduleRequestStatus.APPROVED, component.state.value.requests[0].status)
        assertNull(component.state.value.respondingId)
    }

    @Test
    fun on_reject_updates_request_status() = runTest {
        repo.listResult = Result.success(listOf(stubRequest("r1")))
        repo.respondResult = Result.success(stubRequest("r1", RescheduleRequestStatus.REJECTED))
        val component = createComponent()

        component.onReject("r1")

        assertEquals(RescheduleRequestStatus.REJECTED, component.state.value.requests[0].status)
    }

    @Test
    fun on_approve_passes_approved_decision_to_repository() = runTest {
        repo.listResult = Result.success(listOf(stubRequest("r1")))
        val component = createComponent()
        component.onApprove("r1")
        assertEquals("r1", repo.lastRespondId)
        assertEquals(RescheduleRequestStatus.APPROVED, repo.lastRespondDecision)
    }

    @Test
    fun on_back_emits_back_output() {
        var output: RescheduleQueueComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onBack()
        assertEquals(RescheduleQueueComponent.Output.Back, output)
    }

    @Test
    fun error_propagates_when_load_fails() = runTest {
        repo.listResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        assertEquals("Network error", component.state.value.error)
    }

    @Test
    fun on_retry_reloads_requests() = runTest {
        repo.listResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        repo.listResult = Result.success(listOf(stubRequest("r1")))
        component.onRetry()
        assertEquals(1, component.state.value.requests.size)
        assertNull(component.state.value.error)
    }

    @Test
    fun state_settles_not_loading_via_flow() = runTest {
        repo.listResult = Result.success(listOf(stubRequest("r1")))
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
