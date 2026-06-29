package com.inclinic.app.features.doctor.modality.presentation

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.modality.application.GetModalityChangeRequestUseCase
import com.inclinic.app.features.doctor.modality.application.RespondModalityChangeUseCase
import com.inclinic.app.features.doctor.modality.core.model.ModalityRequestStatus
import com.inclinic.app.features.doctor.modality.core.model.ModalityResponseAction
import com.inclinic.app.features.doctor.modality.fakes.FakeModalityRequestRepository
import com.inclinic.app.features.doctor.modality.fakes.stubModalityRequest
import com.inclinic.app.features.doctor.modality.presentation.component.DefaultRespondModalityComponent
import com.inclinic.app.features.doctor.modality.presentation.component.RespondModalityComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultRespondModalityComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val repo = FakeModalityRequestRepository()

    private fun createComponent(
        requestId: String = "req-1",
        onOutput: (RespondModalityComponent.Output) -> Unit = {},
    ) = DefaultRespondModalityComponent(
        componentContext = ctx,
        requestId = requestId,
        getRequest = GetModalityChangeRequestUseCase(repo, dispatchers),
        respondModality = RespondModalityChangeUseCase(repo, dispatchers),
        dispatchers = dispatchers,
        onOutput = onOutput,
    )

    @Test
    fun loads_request_on_init() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))
        val component = createComponent()
        assertEquals("req-1", component.state.value.request?.id)
        assertFalse(component.state.value.isLoading)
    }

    @Test
    fun passes_request_id_to_get_use_case() = runTest {
        createComponent(requestId = "req-55")
        assertEquals("req-55", repo.lastGetId)
    }

    @Test
    fun error_propagates_when_load_fails() = runTest {
        repo.getRequestResult = Result.failure(RuntimeException("Network error"))
        val component = createComponent()
        assertEquals("Network error", component.state.value.error)
        assertNull(component.state.value.request)
    }

    @Test
    fun on_approve_responds_with_approve_action() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))
        repo.respondResult = Result.success(stubModalityRequest("req-1", ModalityRequestStatus.APPROVED))
        val component = createComponent()

        component.onApprove()

        assertEquals(ModalityResponseAction.APPROVE, repo.lastRespondAction)
        assertEquals(ModalityRequestStatus.APPROVED, component.state.value.request?.status)
    }

    @Test
    fun on_reject_responds_with_reject_action() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))
        repo.respondResult = Result.success(stubModalityRequest("req-1", ModalityRequestStatus.REJECTED))
        val component = createComponent()

        component.onReject()

        assertEquals(ModalityResponseAction.REJECT, repo.lastRespondAction)
        assertEquals(ModalityRequestStatus.REJECTED, component.state.value.request?.status)
    }

    @Test
    fun on_approve_passes_adjusted_price_when_set() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))
        repo.respondResult = Result.success(stubModalityRequest("req-1", ModalityRequestStatus.APPROVED))
        val component = createComponent()

        component.onPriceChange("200")
        component.onApprove()

        assertEquals(200, repo.lastRespondPrice)
    }

    @Test
    fun on_approve_passes_null_price_when_blank() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))
        repo.respondResult = Result.success(stubModalityRequest("req-1", ModalityRequestStatus.APPROVED))
        val component = createComponent()

        component.onApprove()

        assertEquals(null, repo.lastRespondPrice)
    }

    @Test
    fun on_price_change_updates_state() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))
        val component = createComponent()

        component.onPriceChange("150")

        assertEquals("150", component.state.value.adjustedPrice)
    }

    @Test
    fun on_approve_emits_responded_output() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))
        repo.respondResult = Result.success(stubModalityRequest("req-1", ModalityRequestStatus.APPROVED))
        var output: RespondModalityComponent.Output? = null
        val component = createComponent(onOutput = { output = it })

        component.onApprove()

        assertEquals(RespondModalityComponent.Output.Responded, output)
    }

    @Test
    fun respond_failure_sets_error() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))
        repo.respondResult = Result.failure(RuntimeException("Respond failed"))
        val component = createComponent()

        component.onApprove()

        assertEquals("Respond failed", component.state.value.error)
        assertFalse(component.state.value.isResponding)
    }

    @Test
    fun on_retry_reloads_request() = runTest {
        repo.getRequestResult = Result.failure(RuntimeException("boom"))
        val component = createComponent()
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))

        component.onRetry()

        assertEquals("req-1", component.state.value.request?.id)
        assertNull(component.state.value.error)
    }

    @Test
    fun on_back_emits_back_output() {
        var output: RespondModalityComponent.Output? = null
        val component = createComponent(onOutput = { output = it })
        component.onBack()
        assertEquals(RespondModalityComponent.Output.Back, output)
    }

    @Test
    fun state_settles_without_loading_via_flow() = runTest {
        repo.getRequestResult = Result.success(stubModalityRequest("req-1"))

        val component = createComponent()

        component.state.asFlow().test {
            val loaded = expectMostRecentItem()
            assertFalse(loaded.isLoading)
            assertNull(loaded.error)
            assertEquals("req-1", loaded.request?.id)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T : Any> com.arkivanov.decompose.value.Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { trySend(it) }
    awaitClose { cancellation.cancel() }
}
