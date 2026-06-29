package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.value.Value

/**
 * Presentation contract for the "Cuenta Creada" / email-confirmation screen.
 *
 * Shown after a patient successfully registers. Informs them an activation
 * email was sent and offers two actions: go to login or resend the email.
 */
interface AccountCreatedComponent {

    /** The email address the activation link was sent to. */
    val email: String

    /** Reactive UI state (resent flag). */
    val state: Value<AccountCreatedState>

    /** User tapped "Ir a iniciar sesión". */
    fun onGoToLogin()

    /** User tapped "Reenviar correo". */
    fun onResend()

    sealed interface Output {
        /** Navigate to the login screen (replace the backstack). */
        data object GoToLogin : Output

        /** A resend was requested — caller may call the use-case or log. */
        data object ResendEmail : Output
    }
}
