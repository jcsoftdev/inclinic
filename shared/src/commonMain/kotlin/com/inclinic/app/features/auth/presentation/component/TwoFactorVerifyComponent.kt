package com.inclinic.app.features.auth.presentation.component

import com.arkivanov.decompose.value.Value

/**
 * Presentation contract for the 2FA verification screen (step 2 of login).
 */
interface TwoFactorVerifyComponent {
    val state: Value<TwoFactorVerifyState>

    /** Called on every keystroke in the 6-digit code field. */
    fun onCodeChange(code: String)

    /** Called when the user taps "Verificar". */
    fun onVerify()

    /** Called when the user dismisses the error banner. */
    fun onErrorDismissed()

    /** Called when the user taps the back button. */
    fun onBack()
}
