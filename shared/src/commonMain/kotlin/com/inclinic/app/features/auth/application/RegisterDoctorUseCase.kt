package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlinx.coroutines.withContext

class RegisterDoctorUseCase(
    private val remote: AuthRemoteDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        specialtyId: String,
    ): Result<Unit> = withContext(dispatchers.io) {
        val nameError = validateName(name)
        if (nameError != null) return@withContext Result.failure(nameError)
        val emailError = validateEmail(email)
        if (emailError != null) return@withContext Result.failure(emailError)
        val pwError = validatePassword(password)
        if (pwError != null) return@withContext Result.failure(pwError)

        remote.register(
            name = name,
            email = email,
            password = password,
            role = "DOCTOR",
            specialtyId = specialtyId,
        )
    }

    private fun validateName(name: String): AuthError.ValidationError? =
        if (name.isBlank()) AuthError.ValidationError(
            field = AuthError.ValidationError.Field.NAME,
            kind = AuthError.ValidationError.Kind.EMPTY_NAME,
        ) else null

    private fun validateEmail(email: String): AuthError.ValidationError? =
        if (!EMAIL_REGEX.matches(email)) AuthError.ValidationError(
            field = AuthError.ValidationError.Field.EMAIL,
            kind = AuthError.ValidationError.Kind.INVALID_EMAIL,
        ) else null

    private fun validatePassword(password: String): AuthError.ValidationError? =
        if (!PASSWORD_REGEX.matches(password)) AuthError.ValidationError(
            field = AuthError.ValidationError.Field.PASSWORD,
            kind = AuthError.ValidationError.Kind.WEAK_PASSWORD,
        ) else null

    private companion object {
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val PASSWORD_REGEX = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
    }
}
