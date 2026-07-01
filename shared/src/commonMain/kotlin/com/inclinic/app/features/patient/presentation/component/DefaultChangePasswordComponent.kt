package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.profile.application.ChangePasswordUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private const val MIN_PASSWORD_LENGTH = 6

class DefaultChangePasswordComponent(
    componentContext: ComponentContext,
    private val changePassword: ChangePasswordUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ChangePasswordComponent.Output) -> Unit,
) : ChangePasswordComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ChangePasswordState())
    override val state: Value<ChangePasswordState> = _state

    override fun onCurrentPasswordChange(value: String) {
        _state.update { it.copy(currentPassword = value, error = null) }
    }

    override fun onNewPasswordChange(value: String) {
        _state.update { it.copy(newPassword = value, error = null) }
    }

    override fun onConfirmNewPasswordChange(value: String) {
        _state.update { it.copy(confirmNewPassword = value, error = null) }
    }

    override fun onSubmit() {
        val current = _state.value
        if (current.isLoading) return

        // Client-side validation
        val validationError = validate(current)
        if (validationError != null) {
            _state.update { it.copy(error = validationError) }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            changePassword(current.currentPassword, current.newPassword)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, success = true) }
                }
                .onFailure { err ->
                    val message = when {
                        err.message?.contains("INVALID_CREDENTIALS") == true ->
                            "Contraseña actual incorrecta. Inténtalo de nuevo."
                        else -> err.message ?: "Error al cambiar contraseña"
                    }
                    _state.update { it.copy(isLoading = false, error = message) }
                }
        }
    }

    override fun onBack() = onOutput(ChangePasswordComponent.Output.Back)

    private fun validate(state: ChangePasswordState): String? = when {
        state.currentPassword.isBlank() ->
            "Ingresa tu contraseña actual"
        state.newPassword.length < MIN_PASSWORD_LENGTH ->
            "La nueva contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
        state.newPassword != state.confirmNewPassword ->
            "Las contraseñas no coinciden"
        else -> null
    }
}
