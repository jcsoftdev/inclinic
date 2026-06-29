package com.inclinic.app.features.doctor.messages.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.messages.application.GetDoctorChatThreadsUseCase
import com.inclinic.app.features.doctor.messages.core.port.ThreadFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultDoctorChatListComponent(
    componentContext: ComponentContext,
    private val getThreads: GetDoctorChatThreadsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DoctorChatListComponent.Output) -> Unit,
) : DoctorChatListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DoctorChatListState())
    override val state: Value<DoctorChatListState> = _state

    init { load(_state.value.activeFilter) }

    override fun onRefresh() { load(_state.value.activeFilter) }

    override fun onFilterChange(filter: ThreadFilter) {
        _state.update { it.copy(activeFilter = filter) }
        load(filter)
    }

    override fun onThreadClick(threadId: String) {
        onOutput(DoctorChatListComponent.Output.NavigateToConversation(threadId))
    }

    override fun onBack() {
        onOutput(DoctorChatListComponent.Output.Back)
    }

    private fun load(filter: ThreadFilter) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getThreads(filter)
                .onSuccess { threads ->
                    _state.update { it.copy(isLoading = false, threads = threads) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading messages")) }
                }
        }
    }
}
