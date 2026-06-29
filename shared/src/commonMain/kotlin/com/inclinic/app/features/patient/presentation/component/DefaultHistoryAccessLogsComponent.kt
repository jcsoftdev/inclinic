package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.medical_history.application.GetHistoryAccessLogsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultHistoryAccessLogsComponent(
    componentContext: ComponentContext,
    private val getAccessLogs: GetHistoryAccessLogsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (HistoryAccessLogsComponent.Output) -> Unit,
) : HistoryAccessLogsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(HistoryAccessLogsState())
    override val state: Value<HistoryAccessLogsState> = _state

    init { load() }

    override fun onRefresh() { load() }
    override fun onBack() { onOutput(HistoryAccessLogsComponent.Output.Back) }
    override fun onManageAccess() { onOutput(HistoryAccessLogsComponent.Output.NavigateToManageAccess) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAccessLogs()
                .onSuccess { logs -> _state.update { it.copy(isLoading = false, logs = logs) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error")) } }
        }
    }
}
