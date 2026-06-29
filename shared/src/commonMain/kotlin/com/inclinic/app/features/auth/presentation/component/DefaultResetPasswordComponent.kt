package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.ResetPasswordUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultResetPasswordComponent(
    componentContext: ComponentContext,
    override val token: String,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ResetPasswordComponent.Output) -> Unit,
) : ResetPasswordComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(ResetPasswordState())
    override val state: Value<ResetPasswordState> = _state

    override fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, passwordError = null, error = null) }
    }

    override fun onConfirmPasswordChanged(confirmPassword: String) {
        _state.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    override fun onSubmit() {
        val s = _state.value
        if (s.isLoading || s.success) return
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            resetPasswordUseCase(token = token, newPassword = s.password, confirmPassword = s.confirmPassword)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, success = true) }
                    onOutput(ResetPasswordComponent.Output.Success)
                }
                .onFailure { error ->
                    when (error) {
                        is AuthError.ValidationError -> {
                            _state.update { st ->
                                st.copy(
                                    isLoading = false,
                                    passwordError = if (error.field == AuthError.ValidationError.Field.PASSWORD) "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número" else st.passwordError,
                                    confirmPasswordError = if (error.field == AuthError.ValidationError.Field.CONFIRM_PASSWORD) "Las contraseñas no coinciden" else st.confirmPasswordError,
                                )
                            }
                        }
                        is AuthError -> _state.update { it.copy(isLoading = false, error = error) }
                        else -> _state.update { it.copy(isLoading = false, error = AuthError.Unknown(error)) }
                    }
                }
        }
    }

    override fun onBack() {
        onOutput(ResetPasswordComponent.Output.Back)
    }
}
