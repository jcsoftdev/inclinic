package com.inclinic.app.features.admin.specialties.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminSpecialtyRequestItem
import kotlinx.coroutines.withContext

class GetSpecialtyRequestsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<AdminSpecialtyRequestItem>> =
        withContext(dispatchers.io) { dataSource.getSpecialtyRequests() }
}
