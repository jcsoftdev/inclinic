package com.inclinic.app.features.auth.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.core.model.AuthTokens
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult
import com.inclinic.app.features.auth.core.model.UserRole
import com.inclinic.app.features.auth.core.port.AuthRepository
import com.inclinic.app.features.auth.core.port.TokenStorage
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginRequestDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.LoginResponseDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.TwoFactorVerifyResponseDto
import com.inclinic.app.features.auth.infrastructure.remote.dto.UserDto
import kotlinx.coroutines.withContext

/**
 * Hexagonal implementation of [AuthRepository].
 *
 * ## Responsibility boundary
 * [DefaultAuthRepository] orchestrates the remote data source and the token storage port.
 * It does NOT persist tokens after login — that responsibility belongs to [LoginUseCase],
 * which calls [TokenStorage.save] after a successful login. This avoids double-persistence
 * and keeps the repository layer pure (data mapping only).
 *
 * ## Constructor dependencies
 * - [remote]      — [AuthRemoteDataSource] port (Ktor implementation in prod, fake in tests)
 * - [local]       — [TokenStorage] port (platform adapter in prod, fake in tests)
 * - [dispatchers] — [AppDispatchers] for coroutine dispatch (injected for test determinism)
 */
class DefaultAuthRepository(
    private val remote: AuthRemoteDataSource,
    private val local: TokenStorage,
    private val dispatchers: AppDispatchers,
) : AuthRepository {

    /**
     * Delegates to [AuthRemoteDataSource.login], maps DTOs to domain types.
     *
     * Returns [LoginResult.Success] for a normal login, or [LoginResult.TwoFactorRequired]
     * when the server has 2FA enabled for the account.
     *
     * Does NOT call [TokenStorage.save] — token persistence is owned by [LoginUseCase].
     */
    override suspend fun login(credentials: LoginCredentials): Result<LoginResult> =
        withContext(dispatchers.io) {
            remote.login(
                LoginRequestDto(email = credentials.email, password = credentials.password)
            ).map { dto -> dto.toLoginResult() }
        }

    /**
     * Exchanges a [partialToken] + TOTP [code] for full auth tokens.
     * Does NOT persist tokens — callers (UseCase) own that step.
     */
    override suspend fun verifyTwoFactor(partialToken: String, code: String): Result<Pair<AuthUser, AuthTokens>> =
        withContext(dispatchers.io) {
            remote.verifyTwoFactor(partialToken, code).map { dto -> dto.toDomain() }
        }

    /**
     * Clears locally stored tokens. No network call (no server-side revocation in v1).
     */
    override suspend fun logout(): Unit = withContext(dispatchers.io) {
        local.clear()
    }

    override suspend fun isLoggedIn(): Boolean = withContext(dispatchers.io) {
        local.load() != null
    }

    override suspend fun storedTokens(): AuthTokens? = withContext(dispatchers.io) {
        local.load()
    }

    // ── Mapping ──────────────────────────────────────────────────────────────

    /**
     * Maps the flexible [LoginResponseDto] to the sealed [LoginResult].
     *
     * Shape (a): { user, accessToken, refreshToken } → [LoginResult.Success]
     * Shape (b): { requires2FA: true, partialToken }  → [LoginResult.TwoFactorRequired]
     *
     * If the server returns requires2FA without a partialToken we treat it as
     * a malformed response (AuthError.MalformedResponse via the IllegalStateException
     * propagated through Result).
     */
    private fun LoginResponseDto.toLoginResult(): LoginResult {
        if (requires2FA) {
            val token = checkNotNull(partialToken) { "requires2FA=true but partialToken is null" }
            return LoginResult.TwoFactorRequired(token)
        }
        val userDto = checkNotNull(user) { "Missing user in login response" }
        val access  = checkNotNull(accessToken) { "Missing accessToken in login response" }
        val refresh = checkNotNull(refreshToken) { "Missing refreshToken in login response" }
        return LoginResult.Success(
            user = userDto.toAuthUser(),
            tokens = AuthTokens(accessToken = access, refreshToken = refresh),
        )
    }

    private fun TwoFactorVerifyResponseDto.toDomain(): Pair<AuthUser, AuthTokens> =
        Pair(
            user.toAuthUser(),
            AuthTokens(accessToken = accessToken, refreshToken = refreshToken),
        )

    private fun UserDto.toAuthUser() = AuthUser(
        id        = id,
        email     = email,
        firstName = firstName,
        lastName  = lastName,
        role      = role.toUserRole(),
        doctorId  = doctorId,
        patientId = patientId,
    )

    private fun String.toUserRole(): UserRole = runCatching {
        UserRole.valueOf(this)
    }.getOrElse {
        // Unknown roles default to PATIENT in v1 — surfaces as a discoverable value
        // rather than crashing. Track this as tech debt if new roles are added server-side.
        UserRole.PATIENT
    }
}
