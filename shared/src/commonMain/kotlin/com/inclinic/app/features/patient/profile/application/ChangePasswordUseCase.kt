package com.inclinic.app.features.patient.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import kotlinx.coroutines.withContext

/**
 * Changes the authenticated patient's password via PATCH /api/users/me/password.
 *
 * Client-side validation (length, match) is the caller's responsibility.
 * This use case only delegates to the data source, which maps INVALID_CREDENTIALS
 * errors from the backend.
 */
class ChangePasswordUseCase(
    private val dataSource: PatientDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String,
    ): Result<Unit> =
        withContext(dispatchers.io) {
            dataSource.changePassword(currentPassword, newPassword)
        }
}
