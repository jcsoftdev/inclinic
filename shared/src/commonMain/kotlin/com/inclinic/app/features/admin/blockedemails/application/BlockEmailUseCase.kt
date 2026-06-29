package com.inclinic.app.features.admin.blockedemails.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class BlockEmailUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        email: String,
        reason: String,
        durationDays: Int?,
    ): Result<Unit> =
        withContext(dispatchers.io) {
            dataSource.blockEmail(email, reason, durationDays)
        }
}
