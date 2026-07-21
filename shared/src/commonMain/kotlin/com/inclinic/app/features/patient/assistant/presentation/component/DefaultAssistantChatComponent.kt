package com.inclinic.app.features.patient.assistant.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.features.patient.assistant.application.SendAssistantMessageUseCase
import com.inclinic.app.features.patient.assistant.core.error.AssistantError
import com.inclinic.app.features.patient.assistant.core.model.ActiveToolCall
import com.inclinic.app.features.patient.assistant.core.model.AssistantMessage
import com.inclinic.app.features.patient.assistant.core.model.AssistantStreamEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Default [AssistantChatComponent] implementation.
 *
 * ## Coroutine scope lifecycle
 * `essenty-lifecycle-coroutines` is NOT a transitive dependency of Decompose 3.5.0 in this
 * project. We create a [CoroutineScope] manually and cancel it in [doOnDestroy], exactly
 * mirroring the pattern used in [com.inclinic.app.features.auth.presentation.component.DefaultLoginComponent].
 *
 * ## State persistence
 * An [InstanceKeeper] retained object stores a [AssistantChatState] snapshot. On destroy,
 * the current state is written to the snapshot. On recreation (same [InstanceKeeper]),
 * the snapshot is restored so the user sees the same conversation after back-navigation.
 *
 * ## SessionEvents
 * [SessionEvents.emitExpired] is called on UNAUTHORIZED errors — navigates the user to login.
 */
class DefaultAssistantChatComponent(
    componentContext: ComponentContext,
    private val sendMessage: SendAssistantMessageUseCase,
    private val sessionEvents: SessionEvents,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AssistantChatComponent.Output) -> Unit = {},
) : AssistantChatComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    private val retained = instanceKeeper.getOrCreate { RetainedState() }
    private val _state = MutableValue(retained.snapshot ?: AssistantChatState())
    override val state: Value<AssistantChatState> = _state

    private var activeStreamJob: Job? = null
    private var cooldownJob: Job? = null
    private var lastSentMessage: String? = null
    private var lastSentConversationId: String? = null

    // Monotonic counter for message ids. Seeded past any ids already present in a restored
    // snapshot so ids stay unique across back-navigation. Random ids risked LazyColumn key
    // collisions (crash), so a strictly-increasing counter is used instead.
    private var messageIdCounter: Long = _state.value.messages.size.toLong()

    init {
        lifecycle.doOnDestroy {
            retained.snapshot = _state.value
            scope.cancel() // also cancels cooldownJob (child of scope)
        }
    }

    override fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    override fun onSend() {
        val st = _state.value
        if (st.inputText.isBlank() || st.isStreaming || (st.retryAfterSeconds ?: 0) > 0) return
        val msg = st.inputText.trim()
        // Capture before clearing so onRetry can re-send the same payload
        lastSentMessage = msg
        lastSentConversationId = st.conversationId
        _state.update {
            it.copy(
                messages = it.messages + AssistantMessage.User(id = nextId(), text = msg),
                inputText = "",
                isStreaming = true,
                streamingBuffer = "",
                error = null,
            )
        }
        activeStreamJob = scope.launch {
            sendMessage(msg, st.conversationId).collect(::handleEvent)
        }
    }

    override fun onDoctorCardReserve(doctorName: String) {
        _state.update { it.copy(inputText = "Quiero agendar con $doctorName") }
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null, retryAfterSeconds = null) }
    }

    override fun onStop() {
        if (!_state.value.isStreaming) return
        activeStreamJob?.cancel()
        activeStreamJob = null
        _state.update { st ->
            val flushedMessages = if (st.streamingBuffer.isNotEmpty()) {
                st.messages + AssistantMessage.Assistant(id = nextId(), text = st.streamingBuffer)
            } else {
                st.messages
            }
            st.copy(
                messages = flushedMessages,
                streamingBuffer = "",
                isStreaming = false,
                activeToolCall = null,
            )
        }
    }

    override fun onRetry() {
        val msg = lastSentMessage ?: return
        val st = _state.value
        if (st.isStreaming || (st.retryAfterSeconds ?: 0) > 0) return
        _state.update { it.copy(isStreaming = true, streamingBuffer = "", error = null) }
        activeStreamJob = scope.launch {
            sendMessage(msg, st.conversationId).collect(::handleEvent)
        }
    }

    override fun onDisclaimerDismissed() {
        _state.update { it.copy(disclaimerVisible = false) }
    }

    override fun onNavigateToPayment(appointmentId: String) {
        onOutput(AssistantChatComponent.Output.NavigateToPayment(appointmentId))
    }

    // ── Event handling ────────────────────────────────────────────────────────

    private fun handleEvent(event: AssistantStreamEvent) {
        when (event) {
            is AssistantStreamEvent.ConversationIdReceived ->
                _state.update { it.copy(conversationId = event.conversationId) }

            is AssistantStreamEvent.TextDelta ->
                _state.update { it.copy(streamingBuffer = it.streamingBuffer + event.text) }

            is AssistantStreamEvent.ToolCallStarted ->
                _state.update {
                    it.copy(activeToolCall = ActiveToolCall(event.toolCallId, event.toolName))
                }

            is AssistantStreamEvent.ToolResult -> {
                val active = _state.value.activeToolCall
                if (active != null && active.toolCallId == event.toolCallId) {
                    _state.update {
                        it.copy(
                            messages = it.messages + AssistantMessage.ToolResultCard(
                                id = nextId(),
                                toolCallId = event.toolCallId,
                                // Use REAL toolName from activeToolCall — ToolResult event
                                // uses a sentinel because tool-output-available lacks toolName
                                toolName = active.toolName,
                                result = event.result,
                            ),
                            activeToolCall = null,
                        )
                    }
                }
                // If no matching activeToolCall → silently skip (no crash)
            }

            AssistantStreamEvent.Finish -> _state.update { st ->
                val flushedMessages = if (st.streamingBuffer.isNotEmpty()) {
                    st.messages + AssistantMessage.Assistant(id = nextId(), text = st.streamingBuffer)
                } else {
                    st.messages
                }
                st.copy(
                    messages = flushedMessages,
                    streamingBuffer = "",
                    isStreaming = false,
                    activeToolCall = null,
                )
            }

            is AssistantStreamEvent.Error -> handleError(event)
        }
    }

    /**
     * Starts a 1-second-tick countdown coroutine that decrements [AssistantChatState.retryAfterSeconds]
     * from [seconds] to 0, then sets it to null (re-enabling the input bar).
     *
     * Any previous cooldown is cancelled before the new one starts — handles edge cases where
     * the server sends consecutive 429 responses.
     */
    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = scope.launch {
            var remaining = seconds
            while (remaining > 0) {
                _state.update { it.copy(retryAfterSeconds = remaining) }
                delay(1_000L)
                remaining--
            }
            _state.update { it.copy(retryAfterSeconds = null) }
        }
    }

    private fun handleError(event: AssistantStreamEvent.Error) {
        val mapped: AssistantError = when (event.code) {
            "UNAUTHORIZED" -> AssistantError.Unauthorized
            "FORBIDDEN" -> AssistantError.Forbidden
            "VALIDATION", "EMPTY_MESSAGE" -> AssistantError.Validation("message")
            "RATE_LIMIT" -> AssistantError.RateLimit(event.retryAfterSeconds ?: 60)
            "DISABLED" -> AssistantError.Disabled
            "NETWORK" -> AssistantError.Network
            else -> AssistantError.Unknown(null)
        }
        _state.update { st ->
            // W3: Remove the last user bubble on 401 — spec says "no user bubble retained"
            val messages = if (mapped is AssistantError.Unauthorized &&
                st.messages.isNotEmpty() && st.messages.last() is AssistantMessage.User
            ) {
                st.messages.dropLast(1)
            } else {
                st.messages
            }
            st.copy(
                messages = messages,
                error = mapped,
                retryAfterSeconds = (mapped as? AssistantError.RateLimit)?.retryAfterSeconds,
                isStreaming = false,
                activeToolCall = null,
            )
        }
        when (mapped) {
            is AssistantError.RateLimit -> startCooldown(mapped.retryAfterSeconds)
            is AssistantError.Unauthorized -> sessionEvents.emitExpired()
            else -> Unit
        }
    }

    private fun nextId(): String = "msg-${messageIdCounter++}"

    // ── Retained state ────────────────────────────────────────────────────────

    private class RetainedState : InstanceKeeper.Instance {
        var snapshot: AssistantChatState? = null
        override fun onDestroy() {}
    }
}
