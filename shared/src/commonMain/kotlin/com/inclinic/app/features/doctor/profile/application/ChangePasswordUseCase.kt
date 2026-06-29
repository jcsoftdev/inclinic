package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.core.port.DoctorProfileRepository
import kotlinx.coroutines.withContext

/**
 * Changes the authenticated user's password via PATCH /api/users/me/password.
 *
 * Client-side validation (length, match) is the caller's responsibility.
 * This use case only delegates to the repository, which maps INVALID_CREDENTIALS
 * errors from the backend.
 */
class ChangePasswordUseCase(
    private val repository: DoctorProfileRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit> =
        withContext(dispatchers.io) {
            repository.changePassword(currentPassword, newPassword)
        }
}
