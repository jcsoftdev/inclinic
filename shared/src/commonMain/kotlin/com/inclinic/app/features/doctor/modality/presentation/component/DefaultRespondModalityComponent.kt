package com.inclinic.app.features.doctor.modality.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.modality.application.GetModalityChangeRequestUseCase
import com.inclinic.app.features.doctor.modality.application.RespondModalityChangeUseCase
import com.inclinic.app.features.doctor.modality.core.model.ModalityResponseAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRespondModalityComponent(
    componentContext: ComponentContext,
    private val requestId: String,
    private val getRequest: GetModalityChangeRequestUseCase,
    private val respondModality: RespondModalityChangeUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RespondModalityComponent.Output) -> Unit,
) : RespondModalityComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(RespondModalityState())
    override val state: Value<RespondModalityState> = _state

    init { load() }

    override fun onRetry() { load() }

    override fun onPriceChange(value: String) {
        _state.update { it.copy(adjustedPrice = value.filter { ch -> ch.isDigit() }) }
    }

    override fun onApprove() { respond(ModalityResponseAction.APPROVE) }

    override fun onReject() { respond(ModalityResponseAction.REJECT) }

    override fun onBack() {
        onOutput(RespondModalityComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getRequest(requestId)
                .onSuccess { request ->
                    _state.update { it.copy(isLoading = false, request = request, error = null) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando la solicitud")) }
                }
        }
    }

    private fun respond(action: ModalityResponseAction) {
        if (_state.value.isResponding) return
        val price = if (action == ModalityResponseAction.APPROVE) {
            _state.value.adjustedPrice.takeIf { it.isNotBlank() }?.toIntOrNull()
        } else {
            null
        }
        _state.update { it.copy(isResponding = true, error = null) }
        scope.launch {
            respondModality(requestId, action, price)
                .onSuccess { request ->
                    _state.update { it.copy(isResponding = false, request = request) }
                    onOutput(RespondModalityComponent.Output.Responded)
                }
                .onFailure { err ->
                    _state.update { it.copy(isResponding = false, error = err.toUserMessage("Error respondiendo la solicitud")) }
                }
        }
    }
}
