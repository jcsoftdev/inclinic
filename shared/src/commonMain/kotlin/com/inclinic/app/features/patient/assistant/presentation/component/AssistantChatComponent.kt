package com.inclinic.app.features.patient.assistant.presentation.component

import com.arkivanov.decompose.value.Value

/**
 * Public interface for the assistant chat Decompose component.
 *
 * State is observable via [state] — a Decompose [Value] that notifies subscribers
 * on every update. UI layer converts this to a Compose [State] or collects via [asFlow].
 *
 * All `on*` methods are safe to call from the main thread.
 */
interface AssistantChatComponent {

    /** Reactive state observable. */
    val state: Value<AssistantChatState>

    /** Called on every keystroke in the input field. */
    fun onInputChange(text: String)

    /**
     * Attempts to send the current [AssistantChatState.inputText].
     * No-op when: input is blank, [AssistantChatState.isStreaming] is true,
     * or [AssistantChatState.retryAfterSeconds] is > 0.
     */
    fun onSend()

    /**
     * Cancels the in-flight streaming response, flushing whatever partial text
     * arrived so far into a final assistant message. No-op when not streaming.
     */
    fun onStop()

    /**
     * Pre-fills the input field with `"Quiero agendar con {doctorName}"`.
     * Does NOT auto-send — the patient must tap Send manually.
     */
    fun onDoctorCardReserve(doctorName: String)

    /** Dismisses the current [AssistantChatState.error] and clears [AssistantChatState.retryAfterSeconds]. */
    fun onErrorDismissed()

    /**
     * Re-sends the last message that triggered an error.
     *
     * No-op when: no message was sent previously, [AssistantChatState.isStreaming] is true,
     * or [AssistantChatState.retryAfterSeconds] is > 0.
     */
    fun onRetry()

    /** Hides the disclaimer banner for the rest of the session. */
    fun onDisclaimerDismissed()

    /**
     * Navigates to the Payment screen for the given [appointmentId].
     * Emits [Output.NavigateToPayment] — handled by [DefaultPatientFlowComponent].
     */
    fun onNavigateToPayment(appointmentId: String)

    sealed interface Output {
        /** Emitted when the patient taps "Ir a pagar" on [BookingSuccessCard]. */
        data class NavigateToPayment(val appointmentId: String) : Output
    }
}
