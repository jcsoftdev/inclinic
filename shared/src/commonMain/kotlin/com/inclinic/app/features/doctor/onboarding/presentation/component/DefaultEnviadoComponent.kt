package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.doctor.onboarding.application.GetOnboardingStatusUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultEnviadoComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    initialStatus: OnboardingStatus = OnboardingStatus.PENDING,
    private val onOutput: (EnviadoComponent.Output) -> Unit,
) : EnviadoComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(EnviadoState(status = initialStatus))
    override val state: Value<EnviadoState> = _state

    init { refreshStatus() }

    private fun refreshStatus() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            getOnboardingStatusUseCase()
                .onSuccess { status ->
                    _state.update { it.copy(status = status, isLoading = false) }
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.toUserMessage(), isLoading = false) }
                }
        }
    }

    override fun onLogOutClicked() {
        onOutput(EnviadoComponent.Output.LogOut)
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }
}
