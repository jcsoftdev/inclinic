package com.inclinic.app.features.doctor.modality.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.modality.core.model.ModalityChangeRequest

interface RespondModalityComponent {
    val state: Value<RespondModalityState>

    fun onRetry()
    fun onPriceChange(value: String)
    fun onApprove()
    fun onReject()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Responded : Output
    }
}

data class RespondModalityState(
    val isLoading: Boolean = false,
    val request: ModalityChangeRequest? = null,
    val adjustedPrice: String = "",
    val isResponding: Boolean = false,
    val error: String? = null,
)
