package com.inclinic.app.features.doctor.therapy_offers.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.therapy_offers.application.GetMyTherapyOffersUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultTherapyOffersListComponent(
    componentContext: ComponentContext,
    private val getMyOffers: GetMyTherapyOffersUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (TherapyOffersListComponent.Output) -> Unit,
) : TherapyOffersListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    private val _state = MutableValue(TherapyOffersListState(isLoading = true))
    override val state: Value<TherapyOffersListState> = _state

    init {
        lifecycle.doOnCreate { load() }
        lifecycle.doOnDestroy { scope.cancel() }
    }

    override fun onRefresh() { load() }

    override fun onCreateClicked() = onOutput(TherapyOffersListComponent.Output.NavigateToCreate)

    override fun onBack() = onOutput(TherapyOffersListComponent.Output.Back)

    private fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        scope.launch {
            getMyOffers().fold(
                onSuccess = { offers ->
                    _state.value = TherapyOffersListState(offers = offers, isLoading = false)
                },
                onFailure = { err ->
                    _state.value = TherapyOffersListState(error = err.toUserMessage("Error al cargar ofertas"), isLoading = false)
                },
            )
        }
    }
}
