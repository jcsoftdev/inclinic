package com.inclinic.app.features.doctor.negotiation.presentation

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.doctor.negotiation.application.GetPackageNegotiationUseCase
import com.inclinic.app.features.doctor.negotiation.application.RespondPackageNegotiationUseCase
import com.inclinic.app.features.doctor.negotiation.core.model.NegotiationAction
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiationStatus
import com.inclinic.app.features.doctor.negotiation.fakes.FakeDoctorNegotiationRepository
import com.inclinic.app.features.doctor.negotiation.fakes.stubNegotiation
import com.inclinic.app.features.doctor.negotiation.presentation.component.DefaultRespondPackageNegotiationComponent
import com.inclinic.app.features.doctor.negotiation.presentation.component.RespondPackageNegotiationComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DefaultRespondPackageNegotiationComponentTest {

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val ctx = DefaultComponentContext(lifecycle)
    private val dispatchers = TestAppDispatchers()
    private val repo = FakeDoctorNegotiationRepository()
    private var capturedOutput: RespondPackageNegotiationComponent.Output? = null

    private fun createComponent(id: String = "neg-1"): DefaultRespondPackageNegotiationComponent {
        capturedOutput = null
        return DefaultRespondPackageNegotiationComponent(
            componentContext = ctx,
            negotiationId = id,
            getNegotiation = GetPackageNegotiationUseCase(repo, dispatchers),
            respondNegotiation = RespondPackageNegotiationUseCase(repo, dispatchers),
            dispatchers = dispatchers,
            onOutput = { capturedOutput = it },
        )
    }

    // ── Initial load ────────────────────────────────────────────────────────────

    @Test
    fun initial_state_loads_negotiation_on_success() = runTest {
        val target = stubNegotiation("neg-1")
        repo.getResult = Result.success(target)

        val component = createComponent("neg-1")

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.error)
        assertEquals(target, component.state.value.negotiation)
    }

    @Test
    fun initial_state_sets_error_on_failure() = runTest {
        repo.getResult = Result.failure(RuntimeException("Network error"))

        val component = createComponent()

        assertFalse(component.state.value.isLoading)
        assertNull(component.state.value.negotiation)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun passes_id_to_get_use_case() = runTest {
        createComponent("neg-42")
        assertEquals("neg-42", repo.lastGetId)
    }

    // ── Retry ─────────────────────────────────────────────────────────────────

    @Test
    fun onRetry_reloads_negotiation() = runTest {
        repo.getResult = Result.failure(RuntimeException("Error"))
        val component = createComponent("neg-1")
        assertNotNull(component.state.value.error)

        repo.getResult = Result.success(stubNegotiation("neg-1"))
        component.onRetry()

        assertNull(component.state.value.error)
        assertNotNull(component.state.value.negotiation)
        assertEquals(2, repo.getCallCount)
    }

    // ── Accept ────────────────────────────────────────────────────────────────

    @Test
    fun onAccept_responds_with_accept_action_and_emits_Responded() = runTest {
        repo.getResult = Result.success(stubNegotiation("neg-1"))
        repo.respondResult = Result.success(stubNegotiation("neg-1", PackageNegotiationStatus.ACCEPTED))
        val component = createComponent("neg-1")

        component.onAccept()

        assertEquals(NegotiationAction.ACCEPT, repo.lastRespondAction)
        assertNull(repo.lastRespondCounterPriceCents)
        assertEquals(RespondPackageNegotiationComponent.Output.Responded, capturedOutput)
    }

    @Test
    fun onAccept_does_nothing_when_negotiation_not_loaded() = runTest {
        repo.getResult = Result.failure(RuntimeException("Error"))
        val component = createComponent("neg-1")

        component.onAccept()

        assertEquals(0, repo.respondCallCount)
    }

    // ── Reject ────────────────────────────────────────────────────────────────

    @Test
    fun onReject_responds_with_reject_action() = runTest {
        repo.getResult = Result.success(stubNegotiation("neg-1"))
        repo.respondResult = Result.success(stubNegotiation("neg-1", PackageNegotiationStatus.REJECTED))
        val component = createComponent("neg-1")

        component.onReject()

        assertEquals(NegotiationAction.REJECT, repo.lastRespondAction)
        assertEquals(RespondPackageNegotiationComponent.Output.Responded, capturedOutput)
    }

    // ── Counter ───────────────────────────────────────────────────────────────

    @Test
    fun onCounterPriceChange_keeps_only_digits() = runTest {
        repo.getResult = Result.success(stubNegotiation("neg-1"))
        val component = createComponent("neg-1")

        component.onCounterPriceChange("S/ 11,000")

        assertEquals("11000", component.state.value.counterPrice)
    }

    @Test
    fun onSubmitCounter_responds_with_counter_action_and_price() = runTest {
        repo.getResult = Result.success(stubNegotiation("neg-1"))
        repo.respondResult = Result.success(stubNegotiation("neg-1", PackageNegotiationStatus.COUNTERED))
        val component = createComponent("neg-1")
        component.onCounterPriceChange("11000")

        component.onSubmitCounter()

        assertEquals(NegotiationAction.COUNTER, repo.lastRespondAction)
        assertEquals(11000, repo.lastRespondCounterPriceCents)
        assertEquals(RespondPackageNegotiationComponent.Output.Responded, capturedOutput)
    }

    @Test
    fun onSubmitCounter_sets_error_when_price_blank() = runTest {
        repo.getResult = Result.success(stubNegotiation("neg-1"))
        val component = createComponent("neg-1")

        component.onSubmitCounter()

        assertEquals(0, repo.respondCallCount)
        assertNotNull(component.state.value.error)
    }

    @Test
    fun respond_failure_sets_error_and_clears_isResponding() = runTest {
        repo.getResult = Result.success(stubNegotiation("neg-1"))
        repo.respondResult = Result.failure(RuntimeException("Respond failed"))
        val component = createComponent("neg-1")

        component.onAccept()

        assertFalse(component.state.value.isResponding)
        assertNotNull(component.state.value.error)
        assertNull(capturedOutput)
    }

    // ── Navigation ──────────────────────────────────────────────────────────────

    @Test
    fun onBack_emits_Back_output() {
        repo.getResult = Result.success(stubNegotiation("neg-1"))
        val component = createComponent("neg-1")

        component.onBack()

        assertEquals(RespondPackageNegotiationComponent.Output.Back, capturedOutput)
    }

    // ── Flow ──────────────────────────────────────────────────────────────────

    @Test
    fun state_settles_to_loaded_via_flow() = runTest {
        repo.getResult = Result.success(stubNegotiation("neg-1"))

        val component = createComponent("neg-1")

        component.state.asFlow().test {
            val loaded = expectMostRecentItem()
            assertFalse(loaded.isLoading)
            assertNull(loaded.error)
            assertNotNull(loaded.negotiation)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private fun <T : Any> Value<T>.asFlow(): Flow<T> = callbackFlow {
    val cancellation = subscribe { trySend(it) }
    awaitClose { cancellation.cancel() }
}
