package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.application.ResendActivationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Decompose implementation of [AccountCreatedComponent].
 *
 * - [onGoToLogin] emits [Output.GoToLogin]; the auth flow replaces the stack with Login.
 * - [onResend]   calls [ResendActivationUseCase] with the user's email; on success it flips
 *                [AccountCreatedState.isResent] so the screen switches the button label to a
 *                confirmation message and emits [Output.ResendEmail] for observability.
 *                On failure the flag is NOT flipped so the user can retry.
 */
class DefaultAccountCreatedComponent(
    componentContext: ComponentContext,
    override val email: String,
    private val resendActivationUseCase: ResendActivationUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AccountCreatedComponent.Output) -> Unit,
) : AccountCreatedComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(AccountCreatedState())
    override val state: Value<AccountCreatedState> = _state

    override fun onGoToLogin() {
        onOutput(AccountCreatedComponent.Output.GoToLogin)
    }

    override fun onResend() {
        val s = _state.value
        if (s.isResent || s.isLoading) return
        _state.update { it.copy(isLoading = true) }
        scope.launch {
            resendActivationUseCase(email)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isResent = true) }
                    onOutput(AccountCreatedComponent.Output.ResendEmail)
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }
}
