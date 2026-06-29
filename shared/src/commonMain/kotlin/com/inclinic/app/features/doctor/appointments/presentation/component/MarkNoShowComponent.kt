package com.inclinic.app.features.doctor.appointments.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment

interface MarkNoShowComponent {
    val state: Value<MarkNoShowState>

    fun onReasonChanged(value: String)
    fun onConfirm()
    fun onBack()

    sealed interface Output {
        data object Success : Output
        data object Back : Output
    }
}

data class MarkNoShowState(
    val appointment: Appointment? = null,
    val reason: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
) {
    val canConfirm: Boolean get() = reason.trim().length >= MIN_REASON_LENGTH

    companion object {
        const val MIN_REASON_LENGTH = 10
        const val MAX_REASON_LENGTH = 200
    }
}
