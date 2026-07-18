package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.disputes.application.GetDisputesUseCase
import com.inclinic.app.features.admin.disputes.application.ResolveDisputeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminResolveDisputeComponent(
    componentContext: ComponentContext,
    private val disputeId: String,
    private val getDisputes: GetDisputesUseCase,
    private val resolveDispute: ResolveDisputeUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminResolveDisputeComponent.Output) -> Unit,
) : AdminResolveDisputeComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminResolveDisputeState())
    override val state: Value<AdminResolveDisputeState> = _state

    init { load() }

    override fun onBack() {
        onOutput(AdminResolveDisputeComponent.Output.Back)
    }

    override fun onSelectResolution(resolution: String) {
        _state.update { it.copy(selectedResolution = resolution, submitError = null) }
    }

    override fun onNoteChange(note: String) {
        _state.update { it.copy(note = note, submitError = null) }
    }

    override fun onConfirm() {
        val s = _state.value
        val resolution = s.selectedResolution ?: return
        val note = s.note.trim()
        if (note.length < 10) {
            _state.update { it.copy(submitError = "La nota debe tener al menos 10 caracteres") }
            return
        }
        if (s.isSubmitting) return
        _state.update { it.copy(isSubmitting = true, submitError = null) }
        scope.launch {
            resolveDispute(disputeId, resolution, note)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onOutput(AdminResolveDisputeComponent.Output.Back)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, submitError = err.toUserMessage("Error al resolver disputa")) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, loadError = null, notFound = false) }
        scope.launch {
            getDisputes(null)
                .onSuccess { items ->
                    val dispute = items.firstOrNull { it.id == disputeId }
                    if (dispute != null) {
                        _state.update { it.copy(isLoading = false, dispute = dispute) }
                    } else {
                        _state.update { it.copy(isLoading = false, loadError = "Disputa no encontrada", notFound = true) }
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, loadError = err.toUserMessage("Error al cargar disputa")) }
                }
        }
    }
}
