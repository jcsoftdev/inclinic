package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.RescheduleProposal

interface RescheduleResponseComponent {
    val state: Value<RescheduleResponseState>

    fun onAccept()
    fun onReject()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Responded : Output
    }
}

data class RescheduleResponseState(
    val proposal: RescheduleProposal? = null,
    val isLoading: Boolean = false,
    val isResponding: Boolean = false,
    val error: String? = null,
)
