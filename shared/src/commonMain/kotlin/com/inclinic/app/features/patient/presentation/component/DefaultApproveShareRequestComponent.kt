package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.medical_history.application.GetShareRequestDetailUseCase
import com.inclinic.app.features.patient.medical_history.application.RespondShareRequestUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultApproveShareRequestComponent(
    componentContext: ComponentContext,
    private val requestId: String,
    private val getDetail: GetShareRequestDetailUseCase,
    private val respond: RespondShareRequestUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ApproveShareRequestComponent.Output) -> Unit,
) : ApproveShareRequestComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ApproveShareRequestState())
    override val state: Value<ApproveShareRequestState> = _state

    init { load() }

    override fun onDurationSelected(days: Int) { _state.update { it.copy(selectedDuration = days) } }

    override fun onApprove() {
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            respond(requestId, true, _state.value.selectedDuration)
                .onSuccess { onOutput(ApproveShareRequestComponent.Output.Approved) }
                .onFailure { err -> _state.update { it.copy(isSubmitting = false, error = err.toUserMessage()) } }
        }
    }

    override fun onReject() {
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            respond(requestId, false, null)
                .onSuccess { onOutput(ApproveShareRequestComponent.Output.Rejected) }
                .onFailure { err -> _state.update { it.copy(isSubmitting = false, error = err.toUserMessage()) } }
        }
    }

    override fun onClose() { onOutput(ApproveShareRequestComponent.Output.Closed) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getDetail(requestId)
                .onSuccess { req -> _state.update { it.copy(isLoading = false, request = req) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error")) } }
        }
    }
}
