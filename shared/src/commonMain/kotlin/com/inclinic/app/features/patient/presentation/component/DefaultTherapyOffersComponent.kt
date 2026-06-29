package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.therapy.application.GetTherapyOffersUseCase
import com.inclinic.app.features.patient.therapy.application.PurchasePackageUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultTherapyOffersComponent(
    componentContext: ComponentContext,
    private val getTherapyOffers: GetTherapyOffersUseCase,
    private val purchasePackage: PurchasePackageUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (TherapyOffersComponent.Output) -> Unit,
) : TherapyOffersComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(TherapyOffersState())
    override val state: Value<TherapyOffersState> = _state

    init { load() }

    override fun onOfferTapped(offerId: String) {
        // Could navigate to offer detail in the future
    }

    override fun onBuy(offerId: String) {
        if (_state.value.purchasingOfferId != null) return
        _state.update { it.copy(purchasingOfferId = offerId, error = null) }
        scope.launch {
            purchasePackage(offerId)
                .onSuccess { packageId ->
                    _state.update { it.copy(purchasingOfferId = null) }
                    onOutput(TherapyOffersComponent.Output.NavigateToPayment(packageId))
                }
                .onFailure { err ->
                    _state.update { it.copy(purchasingOfferId = null, error = err.toUserMessage("No se pudo comprar el paquete")) }
                }
        }
    }

    override fun onNegotiate(offerId: String) {
        // Block starting a negotiation while a purchase is in flight — otherwise
        // the still-running purchase coroutine can push Payment on top of the
        // negotiation screen (racy double-navigation for the same offer).
        if (_state.value.purchasingOfferId != null) return
        onOutput(TherapyOffersComponent.Output.StartNegotiation(offerId))
    }

    override fun onNegotiationTapped(negotiationId: String) {
        onOutput(TherapyOffersComponent.Output.NavigateToNegotiation(negotiationId))
    }

    override fun onBack() {
        onOutput(TherapyOffersComponent.Output.Back)
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getTherapyOffers()
                .onSuccess { offers ->
                    _state.update { it.copy(isLoading = false, offers = offers) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al cargar ofertas")) }
                }
        }
    }
}
