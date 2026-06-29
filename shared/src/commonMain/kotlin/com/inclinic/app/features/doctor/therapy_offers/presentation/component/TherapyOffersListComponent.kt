package com.inclinic.app.features.doctor.therapy_offers.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.therapy_offers.core.model.TherapyOffer

interface TherapyOffersListComponent {
    val state: Value<TherapyOffersListState>

    fun onRefresh()
    fun onCreateClicked()
    fun onBack()

    sealed interface Output {
        data object NavigateToCreate : Output
        data object Back : Output
    }
}

data class TherapyOffersListState(
    val offers: List<TherapyOffer> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
