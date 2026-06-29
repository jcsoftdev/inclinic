package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.VerifyTwoFactorUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.model.AuthUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Default implementation of [TwoFactorVerifyComponent].
 *
 * Follows the same Decompose/coroutine scope pattern as [DefaultLoginComponent]:
 * manual scope + lifecycle.doOnDestroy cancel (no essenty-lifecycle-coroutines dep).
 */
class DefaultTwoFactorVerifyComponent(
    componentContext: ComponentContext,
    private val partialToken: String,
    private val verifyUseCase: VerifyTwoFactorUseCase,
    private val dispatchers: AppDispatchers,
    private val onVerified: (AuthUser) -> Unit,
    private val onBack: () -> Unit,
) : TwoFactorVerifyComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(TwoFactorVerifyState())
    override val state: Value<TwoFactorVerifyState> = _state

    override fun onCodeChange(code: String) {
        // Accept only digits, max 6 characters.
        val filtered = code.filter { it.isDigit() }.take(6)
        _state.update { it.copy(code = filtered, authError = null) }
    }

    override fun onVerify() {
        val current = _state.value
        if (!current.canVerify) return

        _state.update { it.copy(isSubmitting = true, authError = null) }

        scope.launch {
            verifyUseCase(partialToken = partialToken, code = current.code)
                .onSuccess { user ->
                    _state.update { it.copy(isSubmitting = false) }
                    onVerified(user)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            authError = if (error is AuthError) error else AuthError.Unknown(error),
                        )
                    }
                }
        }
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(authError = null) }
    }

    override fun onBack() = onBack.invoke()
}
