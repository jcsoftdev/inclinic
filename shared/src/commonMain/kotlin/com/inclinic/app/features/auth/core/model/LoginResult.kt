package com.inclinic.app.features.auth.core.model

/**
 * Sealed domain result for the login operation.
 *
 * - [Success]: credentials accepted, tokens + user returned.
 * - [TwoFactorRequired]: server has 2FA enabled; a [partialToken] is provided
 *   to be exchanged for real tokens via a 6-digit TOTP code.
 */
sealed interface LoginResult {
    data class Success(val user: AuthUser, val tokens: AuthTokens) : LoginResult
    data class TwoFactorRequired(val partialToken: String) : LoginResult
}
