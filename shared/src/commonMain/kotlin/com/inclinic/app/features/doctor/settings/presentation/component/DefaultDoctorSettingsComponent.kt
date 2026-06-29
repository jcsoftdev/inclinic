package com.inclinic.app.features.doctor.settings.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.LogoutUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultDoctorSettingsComponent(
    componentContext: ComponentContext,
    private val logout: LogoutUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DoctorSettingsComponent.Output) -> Unit,
) : DoctorSettingsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DoctorSettingsState())
    override val state: Value<DoctorSettingsState> = _state

    override fun onToggleNewAppointments(enabled: Boolean) { _state.update { it.copy(newAppointmentsEnabled = enabled) } }
    override fun onToggleChatMessages(enabled: Boolean) { _state.update { it.copy(chatMessagesEnabled = enabled) } }
    override fun onToggleAppointmentReminders(enabled: Boolean) { _state.update { it.copy(appointmentRemindersEnabled = enabled) } }
    override fun onToggleTwoFactor(enabled: Boolean) { _state.update { it.copy(twoFactorEnabled = enabled) } }

    override fun onBack() { onOutput(DoctorSettingsComponent.Output.Back) }
    override fun onDeleteAccount() { onOutput(DoctorSettingsComponent.Output.NavigateToDeleteAccount) }

    override fun onLogOut() {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLoggingOut = true) }
        scope.launch {
            logout()
            _state.update { it.copy(isLoggingOut = false) }
            onOutput(DoctorSettingsComponent.Output.LoggedOut)
        }
    }
}
