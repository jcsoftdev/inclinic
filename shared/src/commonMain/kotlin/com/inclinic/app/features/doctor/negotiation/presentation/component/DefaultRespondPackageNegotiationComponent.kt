package com.inclinic.app.features.doctor.negotiation.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.negotiation.application.GetPackageNegotiationUseCase
import com.inclinic.app.features.doctor.negotiation.application.RespondPackageNegotiationUseCase
import com.inclinic.app.features.doctor.negotiation.core.model.NegotiationAction
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRespondPackageNegotiationComponent(
    componentContext: ComponentContext,
    private val negotiationId: String,
    private val getNegotiation: GetPackageNegotiationUseCase,
    private val respondNegotiation: RespondPackageNegotiationUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RespondPackageNegotiationComponent.Output) -> Unit,
) : RespondPackageNegotiationComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(RespondPackageNegotiationState())
    override val state: Value<RespondPackageNegotiationState> = _state

    init { load() }

    override fun onRetry() { load() }

    override fun onCounterPriceChange(value: String) {
        _state.update { it.copy(counterPrice = value.filter { ch -> ch.isDigit() }, error = null) }
    }

    override fun onAccept() {
        respond(NegotiationAction.ACCEPT, null)
    }

    override fun onReject() {
        respond(NegotiationAction.REJECT, null)
    }

    override fun onSubmitCounter() {
        val cents = _state.value.counterPrice.toIntOrNull()
        if (cents == null || cents <= 0) {
            _state.update { it.copy(error = "Ingresa un precio válido") }
            return
        }
        respond(NegotiationAction.COUNTER, cents)
    }

    override fun onBack() {
        onOutput(RespondPackageNegotiationComponent.Output.Back)
    }

    private fun respond(action: NegotiationAction, counterPriceCents: Int?) {
        if (_state.value.negotiation == null || _state.value.isResponding) return
        _state.update { it.copy(isResponding = true, error = null) }
        scope.launch {
            respondNegotiation(negotiationId, action, counterPriceCents)
                .onSuccess { updated ->
                    _state.update { it.copy(isResponding = false, negotiation = updated) }
                    onOutput(RespondPackageNegotiationComponent.Output.Responded)
                }
                .onFailure { err ->
                    _state.update { it.copy(isResponding = false, error = err.toUserMessage("Error al responder")) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getNegotiation(negotiationId)
                .onSuccess { negotiation: PackageNegotiation ->
                    _state.update { it.copy(isLoading = false, negotiation = negotiation, error = null) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error al cargar la negociación")) }
                }
        }
    }
}
