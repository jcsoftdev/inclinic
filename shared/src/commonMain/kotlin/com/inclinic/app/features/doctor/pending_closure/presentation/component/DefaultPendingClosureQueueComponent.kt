package com.inclinic.app.features.doctor.pending_closure.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.features.doctor.pending_closure.application.GetPendingClosureQueueUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPendingClosureQueueComponent(
    componentContext: ComponentContext,
    private val getPendingClosureQueue: GetPendingClosureQueueUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PendingClosureQueueComponent.Output) -> Unit,
) : PendingClosureQueueComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PendingClosureQueueState())
    override val state: Value<PendingClosureQueueState> = _state

    init { load() }

    override fun onAppointmentTapped(appointmentId: String) {
        onOutput(PendingClosureQueueComponent.Output.NavigateToDetail(appointmentId))
    }

    override fun onRetry() { load() }

    override fun onBack() { onOutput(PendingClosureQueueComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPendingClosureQueue()
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage()) }
                }
        }
    }
}
