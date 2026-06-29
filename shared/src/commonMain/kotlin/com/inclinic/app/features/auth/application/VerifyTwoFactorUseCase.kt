package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.core.model.AuthUser
import com.inclinic.app.features.auth.core.port.AuthRepository
import com.inclinic.app.features.auth.core.port.TokenStorage
import kotlinx.coroutines.withContext

/**
 * Exchanges a [partialToken] + TOTP [code] for full auth tokens.
 *
 * On success:
 *  1. Persists tokens via [TokenStorage.save].
 *  2. Persists user via [TokenStorage.saveUser].
 *  3. Returns [AuthUser] so the presentation layer can route identically to a
 *     normal (non-2FA) login success.
 */
class VerifyTwoFactorUseCase(
    private val repository: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(partialToken: String, code: String): Result<AuthUser> =
        withContext(dispatchers.io) {
            repository.verifyTwoFactor(partialToken, code).map { (user, tokens) ->
                tokenStorage.save(tokens)
                tokenStorage.saveUser(user)
                user
            }
        }
}
