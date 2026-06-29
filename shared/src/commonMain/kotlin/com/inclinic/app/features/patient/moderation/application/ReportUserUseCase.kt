package com.inclinic.app.features.patient.moderation.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.moderation.core.model.ReportCategory
import com.inclinic.app.features.patient.moderation.infrastructure.remote.ModerationRemoteDataSource
import kotlinx.coroutines.withContext

/**
 * Report a user for moderation review.
 *
 * Validation mirrors the backend contract:
 * - [reason] must be 10..2000 characters.
 */
class ReportUserUseCase(
    private val dataSource: ModerationRemoteDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        userId: String,
        reason: String,
        category: ReportCategory? = null,
    ): Result<Unit> {
        if (reason.length < 10) {
            return Result.failure(IllegalArgumentException("El motivo debe tener al menos 10 caracteres"))
        }
        if (reason.length > 2000) {
            return Result.failure(IllegalArgumentException("El motivo no puede superar los 2000 caracteres"))
        }
        return withContext(dispatchers.io) {
            dataSource.reportUser(userId, reason, category)
        }
    }
}
