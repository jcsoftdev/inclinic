package com.inclinic.app.features.auth.fakes

import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.core.port.AuthRepository

/**
 * Configurable in-memory stub for [AuthRepository].
 * Tests configure [loginResult] / [verifyTwoFactorResult] and [storedTokensResult] before calling.
 * Tracks call counts to assert no-network guarantees.
 */
class FakeAuthRepository : AuthRepository {

    var loginResult: Result<LoginResult> = Result.success(
        LoginResult.Success(
            user = AuthUser(
                id = "user-1",
                email = "test@test.com",
                firstName = "Test",
                lastName = "User",
                role = UserRole.PATIENT,
            ),
            tokens = AuthTokens(accessToken = "access-token", refreshToken = "refresh-token"),
        )
    )

    var verifyTwoFactorResult: Result<Pair<AuthUser, AuthTokens>> = Result.success(
        Pair(
            AuthUser(
                id = "user-1",
                email = "test@test.com",
                firstName = "Test",
                lastName = "User",
                role = UserRole.PATIENT,
            ),
            AuthTokens(accessToken = "access-token", refreshToken = "refresh-token"),
        )
    )

    var storedTokensResult: AuthTokens? = null

    var loginCallCount = 0
    var logoutCallCount = 0
    var verifyTwoFactorCallCount = 0

    override suspend fun login(credentials: LoginCredentials): Result<LoginResult> {
        loginCallCount++
        return loginResult
    }

    override suspend fun verifyTwoFactor(partialToken: String, code: String): Result<Pair<AuthUser, AuthTokens>> {
        verifyTwoFactorCallCount++
        return verifyTwoFactorResult
    }

    override suspend fun logout() {
        logoutCallCount++
    }

    override suspend fun isLoggedIn(): Boolean = storedTokensResult != null

    override suspend fun storedTokens(): AuthTokens? = storedTokensResult
}
