package com.inclinic.app.features.doctor.sharing.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.sharing.application.GetIncomingShareRequestsUseCase
import com.inclinic.app.features.doctor.sharing.application.GetOutgoingShareRequestsUseCase
import com.inclinic.app.features.doctor.sharing.application.RespondShareRequestUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultShareRequestsListComponent(
    componentContext: ComponentContext,
    private val getIncoming: GetIncomingShareRequestsUseCase,
    private val getOutgoing: GetOutgoingShareRequestsUseCase,
    private val cancelRequest: RespondShareRequestUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ShareRequestsListComponent.Output) -> Unit,
) : ShareRequestsListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ShareRequestsListState())
    override val state: Value<ShareRequestsListState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onSelectIncoming() {
        _state.update { it.copy(showIncoming = true) }
    }

    override fun onSelectOutgoing() {
        _state.update { it.copy(showIncoming = false) }
    }

    override fun onCancel(requestId: String) {
        scope.launch {
            cancelRequest(requestId)
                .onSuccess {
                    // Reload to reflect cancelled state
                    load()
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.toUserMessage("Error al cancelar solicitud")) }
                }
        }
    }

    override fun onRequestNew() {
        onOutput(ShareRequestsListComponent.Output.NavigateToRequestShare)
    }

    override fun onBack() {
        onOutput(ShareRequestsListComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val incomingResult = getIncoming()
            val outgoingResult = getOutgoing()
            _state.update { state ->
                state.copy(
                    isLoading = false,
                    incomingRequests = incomingResult.getOrElse { emptyList() },
                    outgoingRequests = outgoingResult.getOrElse { emptyList() },
                    error = incomingResult.exceptionOrNull()?.toUserMessage()
                        ?: outgoingResult.exceptionOrNull()?.toUserMessage(),
                )
            }
        }
    }
}
