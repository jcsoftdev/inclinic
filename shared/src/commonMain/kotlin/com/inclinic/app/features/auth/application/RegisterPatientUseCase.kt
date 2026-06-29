package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.auth.core.error.AuthError
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlinx.coroutines.withContext

class RegisterPatientUseCase(
    private val remote: AuthRemoteDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        phone: String?,
        password: String,
    ): Result<Unit> = withContext(dispatchers.io) {
        val firstNameError = validateFirstName(firstName)
        if (firstNameError != null) return@withContext Result.failure(firstNameError)
        val lastNameError = validateLastName(lastName)
        if (lastNameError != null) return@withContext Result.failure(lastNameError)
        val emailError = validateEmail(email)
        if (emailError != null) return@withContext Result.failure(emailError)
        val pwError = validatePassword(password)
        if (pwError != null) return@withContext Result.failure(pwError)

        remote.registerPatient(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phone = phone?.takeIf { it.isNotBlank() },
            password = password,
        )
    }

    private fun validateFirstName(firstName: String): AuthError.ValidationError? =
        if (firstName.isBlank()) AuthError.ValidationError(
            field = AuthError.ValidationError.Field.NAME,
            kind = AuthError.ValidationError.Kind.EMPTY_NAME,
        ) else null

    private fun validateLastName(lastName: String): AuthError.ValidationError? =
        if (lastName.isBlank()) AuthError.ValidationError(
            field = AuthError.ValidationError.Field.LAST_NAME,
            kind = AuthError.ValidationError.Kind.EMPTY_LAST_NAME,
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
