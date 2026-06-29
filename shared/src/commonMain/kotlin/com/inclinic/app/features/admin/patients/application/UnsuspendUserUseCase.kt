package com.inclinic.app.features.admin.patients.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class UnsuspendUserUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(userId: String): Result<Unit> =
        withContext(dispatchers.io) { dataSource.unsuspendUser(userId) }
}
