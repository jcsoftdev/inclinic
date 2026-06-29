package com.inclinic.app.features.admin.blockedemails.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class UnblockEmailUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(email: String): Result<Unit> =
        withContext(dispatchers.io) {
            dataSource.unblockEmail(email)
        }
}
