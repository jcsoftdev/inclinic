package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.port.TelemetryService
import com.inclinic.app.features.auth.application.LoginUseCase
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Default implementation of [LoginComponent].
 *
 * ## Coroutine scope lifecycle
 * `essenty-lifecycle-coroutines` is NOT a transitive dependency of Decompose 3.5.0 in this
 * project — it must be declared explicitly and is not yet added to libs.versions.toml.
 * We therefore implement the lifecycle-aware scope manually:
 *   1. Create a [CoroutineScope] with `dispatchers.main + SupervisorJob()`.
 *   2. Cancel it in [com.arkivanov.essenty.lifecycle.Lifecycle.doOnDestroy].
 * This is exactly what `essenty-lifecycle-coroutines`'s `coroutineScope()` extension does.
 *
 * ## State
 * [MutableValue] from Decompose is thread-safe. [update] applies changes atomically.
 *
 * ## Validation
 * Email and password are validated inline before calling [LoginUseCase] — no network
 * call is made when validation fails, satisfying the spec requirement.
 */
class DefaultLoginComponent(
    componentContext: ComponentContext,
    private val loginUseCase: LoginUseCase,
    private val dispatchers: AppDispatchers,
    private val telemetry: TelemetryService? = null,
    private val onLoginSucceeded: (AuthUser) -> Unit = {},
    private val onTwoFactorRequired: (partialToken: String) -> Unit = {},
    private val onNavigateForgotPassword: () -> Unit = {},
    private val onNavigateRegister: () -> Unit = {},
) : LoginComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        // Cancel the scope when the component is destroyed (tied to Essenty lifecycle).
        lifecycle.doOnDestroy { scope.cancel() }
        telemetry?.track("screen_view", mapOf("screen" to "Login"))
    }

    private val _state = MutableValue(LoginState())
    override val state: Value<LoginState> = _state

    override fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null) }
    }

    override fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null) }
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(authError = null) }
    }

    override fun onSubmit() {
        val current = _state.value

        // ── Inline validation (synchronous, no network) ──────────────────────
        val emailError = validateEmail(current.email)
        if (emailError != null) {
            _state.update { it.copy(emailError = emailError, isSubmitting = false) }
            return
        }

        val passwordError = validatePassword(current.password)
        if (passwordError != null) {
            _state.update { it.copy(passwordError = passwordError, isSubmitting = false) }
            return
        }

        // ── Submit (async) ────────────────────────────────────────────────────
        _state.update { it.copy(isSubmitting = true, authError = null) }

        scope.launch {
            val credentials = LoginCredentials(
                email = current.email,
                password = current.password,
            )
            loginUseCase(credentials)
                .onSuccess { result ->
                    when (result) {
                        is LoginResult.Success -> {
                            _state.update {
                                it.copy(isSubmitting = false, loginSuccess = true, authError = null)
                            }
                            telemetry?.track("login_success", mapOf("role" to result.user.role.name))
                            onLoginSucceeded(result.user)
                        }
                        is LoginResult.TwoFactorRequired -> {
                            _state.update { it.copy(isSubmitting = false, authError = null) }
                            telemetry?.track("login_2fa_required")
                            onTwoFactorRequired(result.partialToken)
                        }
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            authError = error.toAuthError(),
                        )
                    }
                    telemetry?.track("login_failure")
                }
        }
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    override fun onForgotPassword() = onNavigateForgotPassword()

    override fun onRegister() = onNavigateRegister()

    private fun validateEmail(email: String): String? =
        if (!EMAIL_REGEX.matches(email)) "Invalid email address" else null

    private fun validatePassword(password: String): String? =
        if (password.isEmpty()) "Password cannot be empty" else null

    private fun Throwable.toAuthError(): AuthError =
        if (this is AuthError) this else AuthError.Unknown(this)

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
