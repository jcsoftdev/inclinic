package com.inclinic.app.features.patient.moderation.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.moderation.infrastructure.remote.ModerationRemoteDataSource
import kotlinx.coroutines.withContext

/**
 * Block a user.
 *
 * [reason] is optional; when provided it must be ≤ 500 characters.
 */
class BlockUserUseCase(
    private val dataSource: ModerationRemoteDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        userId: String,
        reason: String? = null,
    ): Result<Unit> {
        val trimmed = reason?.trim()?.ifBlank { null }
        if (trimmed != null && trimmed.length > 500) {
            return Result.failure(IllegalArgumentException("El motivo no puede superar los 500 caracteres"))
        }
        return withContext(dispatchers.io) {
            dataSource.blockUser(userId, trimmed)
        }
    }
}
