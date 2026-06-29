package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.core.model.TherapyOffer

interface TherapyOffersComponent {
    val state: Value<TherapyOffersState>

    fun onOfferTapped(offerId: String)
    fun onBuy(offerId: String)
    fun onNegotiate(offerId: String)
    fun onNegotiationTapped(negotiationId: String)
    fun onBack()
    fun onErrorDismissed()

    sealed interface Output {
        data class NavigateToNegotiation(val negotiationId: String) : Output
        data class StartNegotiation(val offerId: String) : Output
        data class NavigateToPayment(val therapyPackageId: String) : Output
        data object Back : Output
    }
}

data class TherapyOffersState(
    val offers: List<TherapyOffer> = emptyList(),
    val activeNegotiations: List<PackageNegotiation> = emptyList(),
    val isLoading: Boolean = false,
    val purchasingOfferId: String? = null,
    val error: String? = null,
)
