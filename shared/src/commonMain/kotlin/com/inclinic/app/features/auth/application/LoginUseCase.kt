package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.core.model.LoginCredentials
import com.inclinic.app.features.auth.core.model.LoginResult
import com.inclinic.app.features.auth.core.port.AuthRepository
import com.inclinic.app.features.auth.core.port.TokenStorage
import kotlinx.coroutines.withContext

/**
 * Orchestrates the login flow:
 * 1. Validate inputs synchronously (short-circuits before any network call).
 * 2. Delegate to [AuthRepository.login].
 * 3. On [LoginResult.Success]: persist tokens + user, return [LoginResult.Success].
 * 4. On [LoginResult.TwoFactorRequired]: return as-is (no token persistence yet).
 *
 * The caller (component) decides how to handle each branch.
 *
 * Dispatched on [AppDispatchers.io] (injected — satisfies LSP for deterministic tests).
 */
class LoginUseCase(
    private val repository: AuthRepository,
    private val tokenStorage: TokenStorage,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(credentials: LoginCredentials): Result<LoginResult> =
        withContext(dispatchers.io) {
            val validationError = validate(credentials)
            if (validationError != null) {
                return@withContext Result.failure(validationError)
            }

            repository.login(credentials).map { result ->
                if (result is LoginResult.Success) {
                    tokenStorage.save(result.tokens)
                    tokenStorage.saveUser(result.user)
                }
                result
            }
        }

    private fun validate(credentials: LoginCredentials): AuthError.ValidationError? {
        if (!EMAIL_REGEX.matches(credentials.email)) {
            return AuthError.ValidationError(
                field = AuthError.ValidationError.Field.EMAIL,
                kind = AuthError.ValidationError.Kind.INVALID_EMAIL,
            )
        }
        if (credentials.password.isEmpty()) {
            return AuthError.ValidationError(
                field = AuthError.ValidationError.Field.PASSWORD,
                kind = AuthError.ValidationError.Kind.EMPTY_PASSWORD,
            )
        }
        return null
    }

    private companion object {
        // RFC 5322-compatible simplified regex for common use cases.
        // Empty string and strings without '@' are rejected.
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
