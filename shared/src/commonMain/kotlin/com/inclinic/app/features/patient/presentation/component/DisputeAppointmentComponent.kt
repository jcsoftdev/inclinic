package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.DisputeReason

interface DisputeAppointmentComponent {
    val state: Value<DisputeAppointmentState>

    fun onReasonSelected(reason: DisputeReason)
    fun onDetailsChanged(details: String)
    fun onSubmit()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Disputed : Output
    }
}

data class DisputeAppointmentState(
    val appointment: Appointment? = null,
    val selectedReason: DisputeReason? = null,
    val details: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
)
