package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ShareStatus
import com.inclinic.app.features.patient.medical_history.application.GetShareRequestsUseCase
import com.inclinic.app.features.patient.medical_history.application.RespondShareRequestUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultShareRequestsComponent(
    componentContext: ComponentContext,
    private val getShareRequests: GetShareRequestsUseCase,
    private val respondShareRequest: RespondShareRequestUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ShareRequestsComponent.Output) -> Unit,
) : ShareRequestsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ShareRequestsState())
    override val state: Value<ShareRequestsState> = _state

    init { load() }

    override fun onTabSelected(tab: ShareRequestTab) { _state.update { it.copy(selectedTab = tab) } }
    override fun onRefresh() { load() }
    override fun onBack() { onOutput(ShareRequestsComponent.Output.Back) }
    override fun onRequestSelected(requestId: String) { onOutput(ShareRequestsComponent.Output.NavigateToDetail(requestId)) }

    override fun onInlineApprove(requestId: String) {
        if (_state.value.submittingId != null) return
        _state.update { it.copy(submittingId = requestId, error = null) }
        scope.launch {
            respondShareRequest(requestId, approved = true, durationDays = 30)
                .onSuccess { updated ->
                    _state.update { s ->
                        s.copy(
                            submittingId = null,
                            requests = s.requests.map { if (it.id == requestId) updated else it },
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(submittingId = null, error = err.toUserMessage("Error al aprobar")) }
                }
        }
    }

    override fun onInlineReject(requestId: String) {
        if (_state.value.submittingId != null) return
        _state.update { it.copy(submittingId = requestId, error = null) }
        scope.launch {
            respondShareRequest(requestId, approved = false, durationDays = null)
                .onSuccess { updated ->
                    _state.update { s ->
                        s.copy(
                            submittingId = null,
                            requests = s.requests.map { if (it.id == requestId) updated else it },
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(submittingId = null, error = err.toUserMessage("Error al rechazar")) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getShareRequests()
                .onSuccess { list -> _state.update { it.copy(isLoading = false, requests = list) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error")) } }
        }
    }
}
