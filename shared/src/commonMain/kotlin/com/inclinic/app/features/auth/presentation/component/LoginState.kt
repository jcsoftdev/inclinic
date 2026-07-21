package com.inclinic.app.features.auth.presentation.component

import com.inclinic.app.features.auth.core.error.AuthError

/**
 * Immutable UI state for the login screen.
 *
 * ## @Immutable decision
 * We deliberately omit `@Immutable` (from compose-runtime) in shared commonMain.
 * A plain data class is structurally immutable — Compose infers stability via
 * heuristics and treats data classes as stable when all fields are stable types.
 * Adding compose-runtime to shared commonMain as compileOnly would pull in a
 * transitive annotation dep that is not needed here. Option A (omit) is chosen.
 *
 * All fields are immutable primitives or sealed-class values, so Compose will
 * correctly determine the class is stable without the annotation.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val authError: AuthError? = null,
    val loginSuccess: Boolean = false,
    /** True once [AuthError.InactiveAccount]'s resend-activation CTA has succeeded. */
    val resendActivationSent: Boolean = false,
    /**
     * Set once, at construction, when the previous session ended via a real
     * 401/token-expiry (never for an explicit logout). See
     * [com.inclinic.app.core.navigation.PendingSessionMessage].
     */
    val sessionExpiredMessage: String? = null,
) {
    /** True when [authError] is an unverified/inactive account — offer "reenviar activación". */
    val canResendActivation: Boolean get() = authError is AuthError.InactiveAccount

    /** True when [authError] is an administratively suspended account. */
    val isSuspended: Boolean get() = authError is AuthError.SuspendedAccount
}
