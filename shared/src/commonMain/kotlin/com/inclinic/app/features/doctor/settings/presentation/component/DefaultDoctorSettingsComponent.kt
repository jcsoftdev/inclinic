package com.inclinic.app.features.doctor.settings.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.doctor.settings.infrastructure.remote.DoctorSettingsDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultDoctorSettingsComponent(
    componentContext: ComponentContext,
    private val logout: LogoutUseCase,
    private val dispatchers: AppDispatchers,
    private val settingsDataSource: DoctorSettingsDataSource,
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

    // ── MercadoPago ──────────────────────────────────────────────────────────

    override fun onConnectMercadoPago() {
        if (_state.value.isMercadoPagoLoading) return
        _state.update { it.copy(isMercadoPagoLoading = true, mercadoPagoError = null) }
        scope.launch {
            settingsDataSource.getMercadoPagoConnectUrl()
                .onSuccess { url ->
                    _state.update { it.copy(isMercadoPagoLoading = false, mercadoPagoConnectUrl = url) }
                }
                .onFailure { err ->
                    val message = when {
                        err.message?.contains("MP_NOT_CONFIGURED") == true ->
                            "La integración de MercadoPago no está configurada en el servidor."
                        else -> err.toUserMessage("Error al conectar MercadoPago")
                    }
                    _state.update { it.copy(isMercadoPagoLoading = false, mercadoPagoError = message) }
                }
        }
    }

    override fun onMercadoPagoConnectUrlConsumed() {
        _state.update { it.copy(mercadoPagoConnectUrl = null, mercadoPagoConnected = true) }
    }

    override fun onDisconnectMercadoPago() {
        if (_state.value.isMercadoPagoLoading) return
        _state.update { it.copy(isMercadoPagoLoading = true, mercadoPagoError = null) }
        scope.launch {
            settingsDataSource.disconnectMercadoPago()
                .onSuccess {
                    _state.update { it.copy(isMercadoPagoLoading = false, mercadoPagoConnected = false) }
                }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            isMercadoPagoLoading = false,
                            mercadoPagoError = err.toUserMessage("Error al desconectar MercadoPago"),
                        )
                    }
                }
        }
    }
}
