package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.disputes.application.GetNoShowsUseCase
import com.inclinic.app.features.admin.disputes.application.ResolveNoShowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminResolveNoShowComponent(
    componentContext: ComponentContext,
    private val noShowId: String,
    private val getNoShows: GetNoShowsUseCase,
    private val resolveNoShow: ResolveNoShowUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminResolveNoShowComponent.Output) -> Unit,
) : AdminResolveNoShowComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminResolveNoShowState())
    override val state: Value<AdminResolveNoShowState> = _state

    init { load() }

    override fun onBack() {
        onOutput(AdminResolveNoShowComponent.Output.Back)
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
            resolveNoShow(noShowId, resolution, note)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onOutput(AdminResolveNoShowComponent.Output.Back)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, submitError = err.toUserMessage("Error al resolver no-show")) }
                }
        }
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, loadError = null, notFound = false) }
        scope.launch {
            getNoShows()
                .onSuccess { items ->
                    val noShow = items.firstOrNull { it.id == noShowId }
                    if (noShow != null) {
                        _state.update { it.copy(isLoading = false, noShow = noShow) }
                    } else {
                        _state.update { it.copy(isLoading = false, loadError = "No-show no encontrado", notFound = true) }
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, loadError = err.toUserMessage("Error al cargar no-show")) }
                }
        }
    }
}
