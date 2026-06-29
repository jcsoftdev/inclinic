package com.inclinic.app.features.doctor.no_shows.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.features.doctor.no_shows.application.GetNoShowQueueUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultNoShowQueueComponent(
    componentContext: ComponentContext,
    private val getNoShowQueue: GetNoShowQueueUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (NoShowQueueComponent.Output) -> Unit,
) : NoShowQueueComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(NoShowQueueState())
    override val state: Value<NoShowQueueState> = _state

    init {
        load()
    }

    override fun onTabSelected(tab: NoShowTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    override fun onRetry() {
        load()
    }

    override fun onBack() {
        onOutput(NoShowQueueComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getNoShowQueue()
                .onSuccess { queue ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            pending = queue.pending,
                            resolved = queue.resolved,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = err.toUserMessage(),
                        )
                    }
                }
        }
    }
}
