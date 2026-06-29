package com.inclinic.app.features.doctor.negotiation.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiation

interface RespondPackageNegotiationComponent {
    val state: Value<RespondPackageNegotiationState>

    fun onRetry()
    fun onAccept()
    fun onReject()
    fun onCounterPriceChange(value: String)
    fun onSubmitCounter()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Responded : Output
    }
}

data class RespondPackageNegotiationState(
    val isLoading: Boolean = false,
    val negotiation: PackageNegotiation? = null,
    val counterPrice: String = "",
    val isResponding: Boolean = false,
    val error: String? = null,
)
