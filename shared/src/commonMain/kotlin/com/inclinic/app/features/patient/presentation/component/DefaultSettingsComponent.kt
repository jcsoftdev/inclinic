package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.profile.application.GetPatientProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getProfile: GetPatientProfileUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (SettingsComponent.Output) -> Unit,
) : SettingsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(SettingsState())
    override val state: Value<SettingsState> = _state

    init { load() }

    override fun onPushToggle(enabled: Boolean) { _state.update { it.copy(pushEnabled = enabled) } }
    override fun onAnalyticsToggle(enabled: Boolean) { _state.update { it.copy(analyticsEnabled = enabled) } }
    override fun onChangePassword() { onOutput(SettingsComponent.Output.NavigateToChangePassword) }
    override fun onSubscribe() {}
    override fun onBack() { onOutput(SettingsComponent.Output.Back) }
    override fun onDeleteAccount() { onOutput(SettingsComponent.Output.NavigateToDeleteAccount) }

    private fun load() {
        _state.update { it.copy(isLoading = true) }
        scope.launch {
            getProfile(patientId)
                .onSuccess { profile -> _state.update { it.copy(isLoading = false, email = profile.email, emailVerified = true) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage()) } }
        }
    }
}
