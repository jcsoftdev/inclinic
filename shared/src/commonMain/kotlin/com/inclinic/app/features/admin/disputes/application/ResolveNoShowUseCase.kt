package com.inclinic.app.features.admin.disputes.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class ResolveNoShowUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(id: String, resolution: String, note: String): Result<Unit> =
        withContext(dispatchers.io) { dataSource.resolveNoShow(id, resolution, note) }
}
