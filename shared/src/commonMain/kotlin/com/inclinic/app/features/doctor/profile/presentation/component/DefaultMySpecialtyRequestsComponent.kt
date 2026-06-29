package com.inclinic.app.features.doctor.profile.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.application.GetMySpecialtyRequestsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultMySpecialtyRequestsComponent(
    componentContext: ComponentContext,
    private val getMyRequests: GetMySpecialtyRequestsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (MySpecialtyRequestsComponent.Output) -> Unit,
) : MySpecialtyRequestsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    private val _state = MutableValue(MySpecialtyRequestsState())
    override val state: Value<MySpecialtyRequestsState> = _state

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        load()
    }

    override fun onRetry() = load()

    override fun onRequestNew() = onOutput(MySpecialtyRequestsComponent.Output.RequestNew)

    override fun onBack() = onOutput(MySpecialtyRequestsComponent.Output.Back)

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getMyRequests()
                .onSuccess { requests ->
                    _state.update { it.copy(isLoading = false, requests = requests) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(isLoading = false, error = err.toUserMessage("Error loading requests"))
                    }
                }
        }
    }
}
