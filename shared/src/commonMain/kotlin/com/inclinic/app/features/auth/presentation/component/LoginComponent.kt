package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.value.Value

/**
 * Presentation contract for the Login screen.
 *
 * Consumers (Compose UI) observe [state] via `subscribeAsState()` from Decompose
 * extensions-compose. Commands are pure fire-and-forget calls — no return values.
 *
 * The interface is defined in shared commonMain so both Android and iOS
 * can share the same presentation contract without platform coupling.
 */
interface LoginComponent {
    /** Current UI state — updated atomically by the implementation. */
    val state: Value<LoginState>

    /** Called on every keystroke in the email field. */
    fun onEmailChange(email: String)

    /** Called on every keystroke in the password field. */
    fun onPasswordChange(password: String)

    /** Called when the user taps the submit / login button. */
    fun onSubmit()

    /** Called when the user dismisses an auth error banner / dialog. */
    fun onErrorDismissed()

    /** Called when user taps "¿Olvidaste tu contraseña?". */
    fun onForgotPassword()

    /** Called when user taps register link. */
    fun onRegister()
}
