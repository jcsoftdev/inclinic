package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment

interface CancelAppointmentComponent {
    val state: Value<CancelAppointmentState>

    fun onReasonChanged(reason: String)
    fun onConfirmCancel()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Cancelled : Output
    }
}

data class CancelAppointmentState(
    val appointment: Appointment? = null,
    val reason: String = "",
    val isLoading: Boolean = false,
    val isCancelling: Boolean = false,
    val error: String? = null,
    val daysUntil: Int = 0,
    val canCancel: Boolean = false,
)
