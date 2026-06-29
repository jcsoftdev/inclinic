package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.LogoutUseCase
import com.inclinic.app.features.patient.profile.application.DeleteAccountUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultDeleteAccountComponent(
    componentContext: ComponentContext,
    private val deleteAccount: DeleteAccountUseCase,
    private val logout: LogoutUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DeleteAccountComponent.Output) -> Unit,
) : DeleteAccountComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DeleteAccountState())
    override val state: Value<DeleteAccountState> = _state

    override fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }

    override fun onDismissError() { _state.update { it.copy(error = null) } }

    override fun onBack() { onOutput(DeleteAccountComponent.Output.Back) }

    override fun onConfirm() {
        val s = _state.value
        if (s.isDeleting) return
        if (s.password.isBlank()) {
            _state.update { it.copy(error = "Ingresa tu contraseña para confirmar") }
            return
        }
        _state.update { it.copy(isDeleting = true, error = null) }
        scope.launch {
            deleteAccount(s.password)
                .onSuccess {
                    // Limpia tokens → SessionEvents → RootComponent navega a Auth.
                    logout()
                    _state.update { it.copy(isDeleting = false) }
                    onOutput(DeleteAccountComponent.Output.Deleted)
                }
                .onFailure { err ->
                    _state.update { it.copy(isDeleting = false, error = err.toUserMessage("No pudimos eliminar tu cuenta")) }
                }
        }
    }
}
