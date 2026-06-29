package com.inclinic.app.features.auth.core.port

import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult

/**
 * Port (contract) for authentication operations.
 * commonMain depends on this interface; platform adapters implement it via DI.
 * No expect/actual — DI handles platform dispatch.
 */
interface AuthRepository {
    /**
     * Attempt login. Returns [LoginResult.Success] for direct login or
     * [LoginResult.TwoFactorRequired] when the server has 2FA enabled.
     */
    suspend fun login(credentials: LoginCredentials): Result<LoginResult>
    suspend fun verifyTwoFactor(partialToken: String, code: String): Result<Pair<AuthUser, AuthTokens>>
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
    suspend fun storedTokens(): AuthTokens?
}
