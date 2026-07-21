package com.inclinic.app.features.doctor.reschedule_request.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment

interface RequestRescheduleComponent {
    val state: Value<RequestRescheduleState>

    /** Fija la fecha (yyyy-MM-dd) y carga los slots reales del médico para ese día. */
    fun onDateChange(date: String)
    fun onSlotChange(value: String)
    fun onMessageChange(value: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Success : Output
        data object Back : Output
    }
}

data class RequestRescheduleState(
    val isLoading: Boolean = true,
    val appointment: Appointment? = null,
    /** Fecha elegida (yyyy-MM-dd) para la que se cargan los slots. */
    val selectedDate: String = "",
    /** Slots reales del médico en [selectedDate] (solo los libres). */
    val availableSlots: List<String> = emptyList(),
    val isLoadingSlots: Boolean = false,
    val proposedSlot: String = "",
    val message: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    /** Motivo is mandatory and must reach this length, per the reschedule-request design. */
    val messageTrimmed: String get() = message.trim()

    val canSubmit: Boolean
        get() = appointment != null &&
            proposedSlot.isNotBlank() &&
            messageTrimmed.length >= MIN_REASON_LENGTH &&
            !isSubmitting

    companion object {
        const val MIN_REASON_LENGTH = 20
    }
}
