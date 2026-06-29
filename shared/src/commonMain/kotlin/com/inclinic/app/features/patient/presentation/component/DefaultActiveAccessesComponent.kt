package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.medical_history.application.GetActiveAccessesUseCase
import com.inclinic.app.features.patient.medical_history.application.RevokeAccessUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultActiveAccessesComponent(
    componentContext: ComponentContext,
    private val getActiveAccesses: GetActiveAccessesUseCase,
    private val revokeAccess: RevokeAccessUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ActiveAccessesComponent.Output) -> Unit,
) : ActiveAccessesComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ActiveAccessesState())
    override val state: Value<ActiveAccessesState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onBack() { onOutput(ActiveAccessesComponent.Output.Back) }

    override fun onRevoke(requestId: String) {
        if (_state.value.revokingId != null) return
        _state.update { it.copy(revokingId = requestId, error = null) }
        scope.launch {
            revokeAccess(requestId)
                .onSuccess {
                    _state.update { s ->
                        s.copy(
                            revokingId = null,
                            accesses = s.accesses.filter { it.id != requestId },
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(revokingId = null, error = err.toUserMessage("Error al revocar acceso")) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getActiveAccesses()
                .onSuccess { list -> _state.update { it.copy(isLoading = false, accesses = list) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error")) } }
        }
    }
}
