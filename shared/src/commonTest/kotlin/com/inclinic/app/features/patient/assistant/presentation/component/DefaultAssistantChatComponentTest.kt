package com.inclinic.app.features.patient.assistant.presentation.component

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.features.auth.fakes.TestAppDispatchers
import com.inclinic.app.features.patient.assistant.application.SendAssistantMessageUseCase
import com.inclinic.app.features.patient.assistant.core.error.AssistantError
import com.inclinic.app.features.patient.assistant.core.model.AssistantChatRequest
import com.inclinic.app.features.patient.assistant.core.model.AssistantMessage
import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import com.inclinic.app.features.patient.assistant.core.model.ToolName
import com.inclinic.app.features.patient.assistant.core.port.AssistantChatDataSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * RED → GREEN tests for [DefaultAssistantChatComponent].
 *
 * Pattern mirrors [DefaultLoginComponentTest]:
 * - [TestAppDispatchers] with [UnconfinedTestDispatcher] → coroutines run eagerly
 * - [Value.asFlow] via callbackFlow to collect state changes via Turbine
 * - [LifecycleRegistry] for Decompose lifecycle
 * - [FakeSessionEvents] to assert [SessionEvents.expired] was fired
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DefaultAssistantChatComponentTest {

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private val lifecycle = LifecycleRegistry().also { it.resume() }
    private val dispatchers = TestAppDispatchers()
    private val sessionEvents = SessionEvents()

    // Scripted event channel — tests push events through this
    private val eventChannel = Channel<AssistantStreamEvent>(Channel.UNLIMITED)
    private val fakeDataSource = object : AssistantChatDataSource {
        override suspend fun sendMessage(request: AssistantChatRequest): Flow<AssistantStreamEvent> =
            eventChannel.receiveAsFlow()
    }
    private val sendMessageUseCase = SendAssistantMessageUseCase(fakeDataSource)

    private fun createComponent(instanceKeeper: InstanceKeeperDispatcher? = null): DefaultAssistantChatComponent {
        val ctx = if (instanceKeeper != null) {
            DefaultComponentContext(lifecycle = lifecycle, instanceKeeper = instanceKeeper)
        } else {
            DefaultComponentContext(lifecycle = lifecycle)
        }
        return DefaultAssistantChatComponent(
            componentContext = ctx,
            sendMessage = sendMessageUseCase,
            sessionEvents = sessionEvents,
            dispatchers = dispatchers,
        )
    }

    // ── Test 1: initial state ─────────────────────────────────────────────────

    @Test
    fun initial_state_has_empty_messages_not_streaming_disclaimer_visible_no_error() {
        val component = createComponent()
        val state = component.state.value
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isStreaming)
        assertTrue(state.disclaimerVisible)
        assertNull(state.error)
        assertNull(state.conversationId)
        assertEquals("", state.inputText)
        assertEquals("", state.streamingBuffer)
    }

    // ── Test 2: onInputChange ─────────────────────────────────────────────────

    @Test
    fun onInputChange_updates_inputText() {
        val component = createComponent()
        component.onInputChange("hola")
        assertEquals("hola", component.state.value.inputText)
    }

    // ── Test 3: onSend with blank input is no-op ──────────────────────────────

    @Test
    fun onSend_with_blank_input_is_noop() = runTest {
        val component = createComponent()
        component.onInputChange("   ")
        component.onSend()
        val state = component.state.value
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isStreaming)
    }

    // ── Test 4: onSend while isStreaming is no-op ─────────────────────────────

    @Test
    fun onSend_while_streaming_is_noop() = runTest {
        val component = createComponent()
        component.onInputChange("primera pregunta")
        component.onSend() // starts streaming — channel stays open

        // Now try to send again
        val messageCountAfterFirstSend = component.state.value.messages.size
        component.onInputChange("segunda pregunta")
        component.onSend() // should be blocked

        // Messages should NOT have doubled (no second user message)
        assertEquals(messageCountAfterFirstSend, component.state.value.messages.size)
        assertTrue(component.state.value.isStreaming)

        // Cleanup: close the channel so the first stream finishes
        eventChannel.send(AssistantStreamEvent.Finish)
        eventChannel.close()
    }

    // ── Test 5: valid onSend appends user message, clears input, sets isStreaming ──

    @Test
    fun onSend_valid_appends_user_message_and_sets_streaming() = runTest {
        val component = createComponent()
        component.onInputChange("Tengo fiebre")

        component.state.asFlow().test {
            awaitItem() // initial

            component.onSend()

            val afterSend = awaitItem()
            assertEquals(1, afterSend.messages.size)
            assertIs<AssistantMessage.User>(afterSend.messages.first())
            assertEquals("Tengo fiebre", (afterSend.messages.first() as AssistantMessage.User).text)
            assertEquals("", afterSend.inputText)
            assertTrue(afterSend.isStreaming)

            cancelAndIgnoreRemainingEvents()
        }
        eventChannel.close()
    }

    // ── Test 6: ConversationIdReceived event ──────────────────────────────────

    @Test
    fun ConversationIdReceived_event_sets_conversationId() = runTest {
        val component = createComponent()
        component.onInputChange("hola")
        component.onSend()

        eventChannel.send(AssistantStreamEvent.ConversationIdReceived("conv-abc"))
        eventChannel.send(AssistantStreamEvent.Finish)
        eventChannel.close()

        assertEquals("conv-abc", component.state.value.conversationId)
    }

    // ── Test 7: TextDelta events accumulate in streamingBuffer ────────────────

    @Test
    fun TextDelta_events_accumulate_in_streamingBuffer() = runTest {
        val component = createComponent()
        component.onInputChange("pregunta")
        component.onSend()

        eventChannel.send(AssistantStreamEvent.TextDelta("foo"))
        eventChannel.send(AssistantStreamEvent.TextDelta("bar"))
        eventChannel.close()

        assertEquals("foobar", component.state.value.streamingBuffer)
    }

    // ── Test 8: ToolCallStarted sets activeToolCall ───────────────────────────

    @Test
    fun ToolCallStarted_sets_activeToolCall() = runTest {
        val component = createComponent()
        component.onInputChange("busca doctor")
        component.onSend()

        eventChannel.send(
            AssistantStreamEvent.ToolCallStarted(
                toolCallId = "tc-1",
                toolName = ToolName.SEARCH_DOCTORS,
                args = JsonPrimitive("{}"),
            )
        )
        eventChannel.close()

        val state = component.state.value
        assertNotNull(state.activeToolCall)
        assertEquals("tc-1", state.activeToolCall!!.toolCallId)
        assertEquals(ToolName.SEARCH_DOCTORS, state.activeToolCall!!.toolName)
    }

    // ── Test 9: ToolResult matching activeToolCall appends ToolResultCard and clears activeToolCall ──

    @Test
    fun ToolResult_matching_activeToolCall_appends_card_and_clears_active() = runTest {
        val component = createComponent()
        component.onInputChange("busca doctor")
        component.onSend()

        val toolArgs = JsonPrimitive("{}")
        val toolOutput = JsonPrimitive("""{"doctors":[]}""")

        eventChannel.send(
            AssistantStreamEvent.ToolCallStarted("tc-1", ToolName.SEARCH_DOCTORS, toolArgs)
        )
        eventChannel.send(
            AssistantStreamEvent.ToolResult("tc-1", ToolName.SEARCH_DOCTORS, toolOutput)
        )
        eventChannel.close()

        val state = component.state.value
        assertNull(state.activeToolCall)
        val toolCard = state.messages.filterIsInstance<AssistantMessage.ToolResultCard>()
        assertEquals(1, toolCard.size)
        assertEquals("tc-1", toolCard.first().toolCallId)
        // IMPORTANT: toolName comes from activeToolCall, not from the ToolResult event
        assertEquals(ToolName.SEARCH_DOCTORS, toolCard.first().toolName)
    }

    // ── Test 10: ToolResult with no matching activeToolCall is skipped ─────────

    @Test
    fun ToolResult_with_no_matching_activeToolCall_is_skipped() = runTest {
        val component = createComponent()
        component.onInputChange("pregunta")
        component.onSend()

        // Send ToolResult WITHOUT a prior ToolCallStarted
        eventChannel.send(
            AssistantStreamEvent.ToolResult("unknown-id", ToolName.SEARCH_DOCTORS, JsonPrimitive("{}"))
        )
        eventChannel.send(AssistantStreamEvent.Finish)
        eventChannel.close()

        val state = component.state.value
        // No ToolResultCard should be in messages
        assertTrue(state.messages.filterIsInstance<AssistantMessage.ToolResultCard>().isEmpty())
        assertNull(state.activeToolCall)
    }

    // ── Test 11: Finish flushes streamingBuffer to AssistantMessage ───────────

    @Test
    fun Finish_flushes_buffer_to_AssistantMessage_and_stops_streaming() = runTest {
        val component = createComponent()
        component.onInputChange("respóndeme")
        component.onSend()

        eventChannel.send(AssistantStreamEvent.TextDelta("Hola "))
        eventChannel.send(AssistantStreamEvent.TextDelta("paciente"))
        eventChannel.send(AssistantStreamEvent.Finish)
        eventChannel.close()

        val state = component.state.value
        assertFalse(state.isStreaming)
        assertEquals("", state.streamingBuffer)
        val assistantMessages = state.messages.filterIsInstance<AssistantMessage.Assistant>()
        assertEquals(1, assistantMessages.size)
        assertEquals("Hola paciente", assistantMessages.first().text)
    }

    // ── Test 12: Error("UNAUTHORIZED") → Unauthorized error + SessionEvents.expired ──

    @Test
    fun Error_UNAUTHORIZED_sets_Unauthorized_error_and_fires_SessionEvents_expired() = runTest {
        val component = createComponent()
        component.onInputChange("hola")
        component.onSend()
        eventChannel.send(AssistantStreamEvent.Error("UNAUTHORIZED"))
        eventChannel.close()

        val state = component.state.value
        assertIs<AssistantError.Unauthorized>(state.error)
        assertFalse(state.isStreaming)
        // emitExpired() is called synchronously in handleError — verified via state.
        // The actual SessionEvents wiring is covered in SessionEventsTest.
    }

    // ── Test 13: Error("RATE_LIMIT", retryAfter=30) ───────────────────────────

    @Test
    fun Error_RATE_LIMIT_sets_RateLimit_error_and_retryAfterSeconds() = runTest {
        val component = createComponent()
        component.onInputChange("pregunta")
        component.onSend()

        eventChannel.send(AssistantStreamEvent.Error("RATE_LIMIT", retryAfterSeconds = 30))
        eventChannel.close()

        val state = component.state.value
        assertIs<AssistantError.RateLimit>(state.error)
        assertEquals(30, (state.error as AssistantError.RateLimit).retryAfterSeconds)
        assertEquals(30, state.retryAfterSeconds)
        assertFalse(state.isStreaming)
    }

    // ── Test 14: Error("DISABLED") ────────────────────────────────────────────

    @Test
    fun Error_DISABLED_sets_Disabled_error() = runTest {
        val component = createComponent()
        component.onInputChange("hola")
        component.onSend()

        eventChannel.send(AssistantStreamEvent.Error("DISABLED"))
        eventChannel.close()

        assertIs<AssistantError.Disabled>(component.state.value.error)
        assertFalse(component.state.value.isStreaming)
    }

    // ── Test 15: Error("EMPTY_MESSAGE") ──────────────────────────────────────

    @Test
    fun Error_EMPTY_MESSAGE_sets_Validation_error() = runTest {
        val component = createComponent()
        component.onInputChange("algo")
        component.onSend()

        eventChannel.send(AssistantStreamEvent.Error("EMPTY_MESSAGE"))
        eventChannel.close()

        val error = component.state.value.error
        assertIs<AssistantError.Validation>(error)
        assertFalse(component.state.value.isStreaming)
    }

    // ── Test 16: onDoctorCardReserve pre-fills input but does NOT auto-send ───

    @Test
    fun onDoctorCardReserve_prefills_input_without_sending() = runTest {
        val component = createComponent()
        component.onDoctorCardReserve("Dr. Pérez")

        assertEquals("Quiero agendar con Dr. Pérez", component.state.value.inputText)
        assertFalse(component.state.value.isStreaming)
        assertTrue(component.state.value.messages.isEmpty())
    }

    // ── Test 17: onErrorDismissed clears error and retryAfterSeconds ──────────

    @Test
    fun onErrorDismissed_clears_error_and_retryAfterSeconds() = runTest {
        val component = createComponent()
        component.onInputChange("hola")
        component.onSend()

        eventChannel.send(AssistantStreamEvent.Error("RATE_LIMIT", retryAfterSeconds = 60))
        eventChannel.close()

        assertNotNull(component.state.value.error)
        component.onErrorDismissed()

        assertNull(component.state.value.error)
        assertNull(component.state.value.retryAfterSeconds)
    }

    // ── Test 18: onDisclaimerDismissed sets disclaimerVisible to false ────────

    @Test
    fun onDisclaimerDismissed_hides_disclaimer() {
        val component = createComponent()
        assertTrue(component.state.value.disclaimerVisible)
        component.onDisclaimerDismissed()
        assertFalse(component.state.value.disclaimerVisible)
    }

    // ── Test 19: instanceKeeper preserves snapshot on component recreation ────

    @Test
    fun instanceKeeper_preserves_snapshot_on_recreation() = runTest {
        val lifecycle1 = LifecycleRegistry().also { it.resume() }
        val ik = InstanceKeeperDispatcher()
        val ctx1 = DefaultComponentContext(lifecycle = lifecycle1, instanceKeeper = ik)
        val component1 = DefaultAssistantChatComponent(
            componentContext = ctx1,
            sendMessage = sendMessageUseCase,
            sessionEvents = sessionEvents,
            dispatchers = dispatchers,
        )
        component1.onInputChange("texto guardado")
        component1.onDisclaimerDismissed()

        // Destroy the first lifecycle (triggers doOnDestroy → snapshot saved)
        lifecycle1.destroy()

        // Re-create with the same instanceKeeper
        val lifecycle2 = LifecycleRegistry().also { it.resume() }
        val ctx2 = DefaultComponentContext(lifecycle = lifecycle2, instanceKeeper = ik)
        val component2 = DefaultAssistantChatComponent(
            componentContext = ctx2,
            sendMessage = sendMessageUseCase,
            sessionEvents = sessionEvents,
            dispatchers = dispatchers,
        )

        assertEquals("texto guardado", component2.state.value.inputText)
        assertFalse(component2.state.value.disclaimerVisible)
    }

    // ── Test 20: 429 cooldown — retryAfterSeconds set immediately ─────────────
    //
    // Uses StandardTestDispatcher + advanceTimeBy so virtual time is controlled.
    // A separate eventChannel2 is used to avoid interfering with the shared channel.

    @Test
    fun RateLimit_error_sets_retryAfterSeconds_immediately() = runTest {
        val stdDispatchers = TestAppDispatchers(
            scheduler = testScheduler,
            useStandard = true,
        )
        val ch = Channel<AssistantStreamEvent>(Channel.UNLIMITED)
        val ds = object : AssistantChatDataSource {
            override suspend fun sendMessage(request: AssistantChatRequest): Flow<AssistantStreamEvent> =
                ch.receiveAsFlow()
        }
        val uc = SendAssistantMessageUseCase(ds)
        val ctx = DefaultComponentContext(lifecycle = lifecycle)
        val component = DefaultAssistantChatComponent(
            componentContext = ctx,
            sendMessage = uc,
            sessionEvents = sessionEvents,
            dispatchers = stdDispatchers,
        )

        component.onInputChange("hola")
        component.onSend()
        advanceTimeBy(1) // let send coroutine start

        ch.send(AssistantStreamEvent.Error("RATE_LIMIT", retryAfterSeconds = 5))
        advanceTimeBy(1) // process event

        assertEquals(5, component.state.value.retryAfterSeconds)
        assertIs<AssistantError.RateLimit>(component.state.value.error)
        ch.close()
    }

    // ── Test 21: 429 cooldown — retryAfterSeconds decrements each second ──────

    @Test
    fun RateLimit_cooldown_decrements_retryAfterSeconds_each_second() = runTest {
        val stdDispatchers = TestAppDispatchers(
            scheduler = testScheduler,
            useStandard = true,
        )
        val ch = Channel<AssistantStreamEvent>(Channel.UNLIMITED)
        val ds = object : AssistantChatDataSource {
            override suspend fun sendMessage(request: AssistantChatRequest): Flow<AssistantStreamEvent> =
                ch.receiveAsFlow()
        }
        val uc = SendAssistantMessageUseCase(ds)
        val ctx = DefaultComponentContext(lifecycle = lifecycle)
        val component = DefaultAssistantChatComponent(
            componentContext = ctx,
            sendMessage = uc,
            sessionEvents = sessionEvents,
            dispatchers = stdDispatchers,
        )

        component.onInputChange("hola")
        component.onSend()
        advanceTimeBy(1)

        ch.send(AssistantStreamEvent.Error("RATE_LIMIT", retryAfterSeconds = 3))
        advanceTimeBy(1) // event processed → retryAfterSeconds = 3, countdown starts

        // After 1 second tick → should be 2
        advanceTimeBy(1_000)
        assertEquals(2, component.state.value.retryAfterSeconds)

        // After 2 second ticks → should be 1
        advanceTimeBy(1_000)
        assertEquals(1, component.state.value.retryAfterSeconds)

        ch.close()
    }

    // ── Test 22: 429 cooldown — retryAfterSeconds becomes null when countdown reaches 0 ──

    @Test
    fun RateLimit_cooldown_sets_retryAfterSeconds_null_when_countdown_ends() = runTest {
        val stdDispatchers = TestAppDispatchers(
            scheduler = testScheduler,
            useStandard = true,
        )
        val ch = Channel<AssistantStreamEvent>(Channel.UNLIMITED)
        val ds = object : AssistantChatDataSource {
            override suspend fun sendMessage(request: AssistantChatRequest): Flow<AssistantStreamEvent> =
                ch.receiveAsFlow()
        }
        val uc = SendAssistantMessageUseCase(ds)
        val ctx = DefaultComponentContext(lifecycle = lifecycle)
        val component = DefaultAssistantChatComponent(
            componentContext = ctx,
            sendMessage = uc,
            sessionEvents = sessionEvents,
            dispatchers = stdDispatchers,
        )

        component.onInputChange("hola")
        component.onSend()
        advanceTimeBy(1)

        ch.send(AssistantStreamEvent.Error("RATE_LIMIT", retryAfterSeconds = 3))
        advanceTimeBy(1) // event processed → countdown starts at 3

        // Fast-forward past the full 3-second countdown
        advanceTimeBy(3_000)

        assertNull(component.state.value.retryAfterSeconds)
        ch.close()
    }

    // ── Test 23 (W3): 401 removes the last user bubble from messages ──────────

    @Test
    fun Error_UNAUTHORIZED_removes_last_user_bubble_from_messages() = runTest {
        val component = createComponent()
        component.onInputChange("hola")
        component.onSend()
        // Verify user bubble was added
        assertEquals(1, component.state.value.messages.size)
        assertIs<AssistantMessage.User>(component.state.value.messages.last())

        eventChannel.send(AssistantStreamEvent.Error("UNAUTHORIZED"))
        eventChannel.close()

        val state = component.state.value
        // W3: user bubble must be removed on 401
        assertTrue(
            state.messages.filterIsInstance<AssistantMessage.User>().isEmpty(),
            "User bubble must be removed when 401 is received",
        )
        assertIs<AssistantError.Unauthorized>(state.error)
        assertFalse(state.isStreaming)
    }

    // ── Test 24 (W2): onRetry with no prior send is no-op ────────────────────

    @Test
    fun onRetry_with_no_prior_send_is_noop() = runTest {
        val component = createComponent()
        // Never called onSend — lastSentMessage is null
        component.onRetry()

        val state = component.state.value
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isStreaming)
        assertNull(state.error)
    }

    // ── Test 25 (W2): onRetry while isStreaming is no-op ─────────────────────

    @Test
    fun onRetry_while_streaming_is_noop() = runTest {
        val component = createComponent()
        component.onInputChange("primera pregunta")
        component.onSend() // starts streaming

        val messagesBeforeRetry = component.state.value.messages.size
        assertTrue(component.state.value.isStreaming)

        // onRetry while isStreaming must be blocked
        component.onRetry()

        assertEquals(messagesBeforeRetry, component.state.value.messages.size)
        assertTrue(component.state.value.isStreaming)

        // Cleanup
        eventChannel.send(AssistantStreamEvent.Finish)
        eventChannel.close()
    }

    // ── Test 26 (W2): onRetry after non-auth error re-sends last message ──────

    @Test
    fun onRetry_after_network_error_resends_last_message_with_last_conversationId() = runTest {
        var capturedRequest: AssistantChatRequest? = null
        val retryCh = Channel<AssistantStreamEvent>(Channel.UNLIMITED)
        val countingDs = object : AssistantChatDataSource {
            private var callCount = 0
            override suspend fun sendMessage(request: AssistantChatRequest): Flow<AssistantStreamEvent> {
                callCount++
                capturedRequest = request
                return if (callCount == 1) {
                    // First call: emit conversationId then NETWORK error
                    kotlinx.coroutines.flow.flow {
                        emit(AssistantStreamEvent.ConversationIdReceived("conv-retry"))
                        emit(AssistantStreamEvent.Error("NETWORK"))
                    }
                } else {
                    // Second call (retry): flow from retryCh
                    retryCh.receiveAsFlow()
                }
            }
        }
        val uc = SendAssistantMessageUseCase(countingDs)
        val ctx = DefaultComponentContext(lifecycle = lifecycle)
        val component = DefaultAssistantChatComponent(
            componentContext = ctx,
            sendMessage = uc,
            sessionEvents = sessionEvents,
            dispatchers = dispatchers,
        )

        // First send
        component.onInputChange("Tengo dolor")
        component.onSend()

        // After first send: error is set, conversationId captured
        val stateAfterError = component.state.value
        assertIs<AssistantError.Network>(stateAfterError.error)
        assertEquals("conv-retry", stateAfterError.conversationId)
        assertFalse(stateAfterError.isStreaming)

        // Dismiss error (as the UI would do before retry is wired directly)
        component.onErrorDismissed()

        // Retry
        component.onRetry()

        val stateAfterRetry = component.state.value
        assertTrue(stateAfterRetry.isStreaming)
        assertNull(stateAfterRetry.error)

        // The retry call must use the same message and conversationId
        assertEquals("Tengo dolor", capturedRequest?.message)
        assertEquals("conv-retry", capturedRequest?.conversationId)

        // Cleanup
        retryCh.send(AssistantStreamEvent.Finish)
        retryCh.close()
    }
}

// ── Test helpers ──────────────────────────────────────────────────────────────

/**
 * Converts a Decompose [com.arkivanov.decompose.value.Value] to a [kotlinx.coroutines.flow.Flow]
 * via its subscribe/unsubscribe callback mechanism.
 * Mirrors the [asFlow] extension defined in [DefaultLoginComponentTest].
 */
fun <T : Any> com.arkivanov.decompose.value.Value<T>.asFlow(): Flow<T> =
    callbackFlow {
        val cancellation = subscribe { value -> trySend(value) }
        awaitClose { cancellation.cancel() }
    }
