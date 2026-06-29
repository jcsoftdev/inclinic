package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlinx.coroutines.withContext

class ResetPasswordUseCase(
    private val remote: AuthRemoteDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(token: String, newPassword: String, confirmPassword: String): Result<Unit> =
        withContext(dispatchers.io) {
            if (!PASSWORD_REGEX.matches(newPassword)) {
                return@withContext Result.failure(
                    AuthError.ValidationError(
                        field = AuthError.ValidationError.Field.PASSWORD,
                        kind = AuthError.ValidationError.Kind.WEAK_PASSWORD,
                    )
                )
            }
            if (newPassword != confirmPassword) {
                return@withContext Result.failure(
                    AuthError.ValidationError(
                        field = AuthError.ValidationError.Field.CONFIRM_PASSWORD,
                        kind = AuthError.ValidationError.Kind.PASSWORD_MISMATCH,
                    )
                )
            }
            remote.resetPassword(token, newPassword)
        }

    private companion object {
        val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
    }
}
