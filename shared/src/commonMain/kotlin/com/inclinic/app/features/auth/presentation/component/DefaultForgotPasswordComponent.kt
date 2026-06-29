package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.ForgotPasswordUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultForgotPasswordComponent(
    componentContext: ComponentContext,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ForgotPasswordComponent.Output) -> Unit,
) : ForgotPasswordComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(ForgotPasswordState())
    override val state: Value<ForgotPasswordState> = _state

    override fun onEmailChanged(email: String) {
        _state.update { it.copy(email = email, emailError = null, error = null) }
    }

    override fun onSubmit() {
        val s = _state.value
        if (s.isLoading || s.isSent) return
        if (!EMAIL_REGEX.matches(s.email)) {
            _state.update { it.copy(emailError = "Email no válido") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            forgotPasswordUseCase(s.email)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isSent = true) }
                }
                .onFailure { error ->
                    // Security: always show success message regardless of server error
                    // Exception: network/server errors surface normally so user knows to retry
                    val authError = error as? AuthError ?: AuthError.Unknown(error)
                    when (authError) {
                        is AuthError.NetworkError, is AuthError.ServerError ->
                            _state.update { it.copy(isLoading = false, error = authError) }
                        else ->
                            _state.update { it.copy(isLoading = false, isSent = true) }
                    }
                }
        }
    }

    override fun onBack() {
        onOutput(ForgotPasswordComponent.Output.Back)
    }

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
