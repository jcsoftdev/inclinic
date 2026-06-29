package com.inclinic.app.features.auth.presentation.component

import com.inclinic.app.features.auth.core.error.AuthError

/**
 * Immutable UI state for the 2FA verification screen.
 */
data class TwoFactorVerifyState(
    val code: String = "",
    val isSubmitting: Boolean = false,
    val authError: AuthError? = null,
) {
    /** Verify button is enabled only when all 6 digits are filled and not loading. */
    val canVerify: Boolean get() = code.length == 6 && !isSubmitting
}
