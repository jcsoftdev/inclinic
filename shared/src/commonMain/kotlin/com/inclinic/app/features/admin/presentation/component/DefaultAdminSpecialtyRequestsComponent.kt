package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.specialties.application.GetSpecialtyRequestsUseCase
import com.inclinic.app.features.admin.specialties.application.ResolveSpecialtyRequestUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminSpecialtyRequestsComponent(
    componentContext: ComponentContext,
    private val getRequests: GetSpecialtyRequestsUseCase,
    private val resolveRequest: ResolveSpecialtyRequestUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminSpecialtyRequestsComponent.Output) -> Unit,
) : AdminSpecialtyRequestsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminSpecialtyRequestsState())
    override val state: Value<AdminSpecialtyRequestsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onOpenEvaluate(requestId: String) {
        _state.update { it.copy(evaluatingRequestId = requestId, selectedAction = null, reason = "", submitError = null) }
    }

    override fun onDismissEvaluate() {
        _state.update { it.copy(evaluatingRequestId = null, selectedAction = null, reason = "", submitError = null) }
    }

    override fun onSelectAction(action: String) {
        _state.update { it.copy(selectedAction = action, submitError = null) }
    }

    override fun onReasonChange(reason: String) {
        _state.update { it.copy(reason = reason) }
    }

    override fun onConfirmEvaluate() {
        val st = _state.value
        val requestId = st.evaluatingRequestId ?: return
        val action = st.selectedAction ?: return

        if (action == "reject" && st.reason.trim().length < 10) {
            _state.update { it.copy(submitError = "La razón debe tener al menos 10 caracteres") }
            return
        }

        _state.update { it.copy(isSubmitting = true, submitError = null) }
        scope.launch {
            resolveRequest(
                requestId = requestId,
                action = action,
                reason = if (action == "reject") st.reason.trim() else null,
            )
                .onSuccess {
                    // Remove the resolved request from the list and close the sheet
                    _state.update { s ->
                        s.copy(
                            isSubmitting = false,
                            evaluatingRequestId = null,
                            selectedAction = null,
                            reason = "",
                            items = s.items.filter { it.id != requestId },
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, submitError = err.toUserMessage("Error procesando solicitud")) }
                }
        }
    }

    override fun onBack() {
        onOutput(AdminSpecialtyRequestsComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getRequests()
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando solicitudes")) }
                }
        }
    }
}
