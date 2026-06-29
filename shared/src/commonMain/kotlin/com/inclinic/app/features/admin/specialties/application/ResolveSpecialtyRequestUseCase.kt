package com.inclinic.app.features.admin.specialties.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class ResolveSpecialtyRequestUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        requestId: String,
        action: String,   // "approve" | "reject"
        reason: String?,
    ): Result<Unit> =
        withContext(dispatchers.io) { dataSource.resolveSpecialtyRequest(requestId, action, reason) }
}
