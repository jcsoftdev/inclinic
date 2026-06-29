package com.inclinic.app.features.doctor.profile.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.application.GetDoctorIncomeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultIncomeComponent(
    componentContext: ComponentContext,
    private val getIncome: GetDoctorIncomeUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (IncomeComponent.Output) -> Unit,
) : IncomeComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    private val _state = MutableValue(IncomeState())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
        load()
    }

    override val state: Value<IncomeState> = _state

    override fun onRetry() { load() }

    override fun onBack() = onOutput(IncomeComponent.Output.Back)

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getIncome()
                .onSuccess { summary ->
                    _state.update { it.copy(isLoading = false, summary = summary) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading income")) }
                }
        }
    }
}
