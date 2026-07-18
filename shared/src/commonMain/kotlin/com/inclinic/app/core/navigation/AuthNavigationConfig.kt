package com.inclinic.app.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthNavigationConfig {
    @Serializable data object Login : AuthNavigationConfig
    @Serializable data object RegisterChooser : AuthNavigationConfig
    @Serializable data object RegisterPatient : AuthNavigationConfig
    @Serializable data object RegisterDoctor : AuthNavigationConfig
    @Serializable data object ForgotPassword : AuthNavigationConfig
    @Serializable data class ResetPassword(val token: String) : AuthNavigationConfig
    @Serializable data class Activate(val email: String) : AuthNavigationConfig
    @Serializable data class AccountCreated(val email: String) : AuthNavigationConfig
    /** Step 2 of login when 2FA is enabled. Carries the partial token from step 1. */
    @Serializable data class TwoFactorVerify(val partialToken: String) : AuthNavigationConfig
    /** Standalone HTTP 429 "too many attempts" screen — reached from Login on [com.inclinic.app.features.auth.core.error.AuthError.TooManyAttempts]. */
    @Serializable data object RateLimit : AuthNavigationConfig
}
