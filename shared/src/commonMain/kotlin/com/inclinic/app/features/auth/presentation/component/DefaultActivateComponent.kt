package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.ActivateUseCase
import com.inclinic.app.features.auth.application.ResendActivationUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val RESEND_COOLDOWN_SECONDS = 60

class DefaultActivateComponent(
    componentContext: ComponentContext,
    override val email: String,
    private val activateUseCase: ActivateUseCase,
    private val resendActivationUseCase: ResendActivationUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ActivateComponent.Output) -> Unit,
) : ActivateComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(ActivateState())
    override val state: Value<ActivateState> = _state

    override fun onCodeChanged(code: String) {
        _state.update { it.copy(code = code, error = null) }
    }

    override fun onSubmit() {
        val s = _state.value
        if (s.isLoading || s.code.isBlank()) return
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            activateUseCase(s.code)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isSent = true) }
                    onOutput(ActivateComponent.Output.Success)
                }
                .onFailure { error ->
                    val authError = error as? AuthError ?: AuthError.Unknown(error)
                    _state.update { it.copy(isLoading = false, error = authError) }
                }
        }
    }

    override fun onResend() {
        val s = _state.value
        if (s.resendCooldownSeconds > 0 || s.isLoading) return
        scope.launch {
            resendActivationUseCase(email)
            startCooldown()
        }
    }

    override fun onBack() {
        onOutput(ActivateComponent.Output.Back)
    }

    private fun startCooldown() {
        scope.launch {
            var remaining = RESEND_COOLDOWN_SECONDS
            while (remaining > 0) {
                _state.update { it.copy(resendCooldownSeconds = remaining) }
                delay(1_000)
                remaining--
            }
            _state.update { it.copy(resendCooldownSeconds = 0) }
        }
    }
}
