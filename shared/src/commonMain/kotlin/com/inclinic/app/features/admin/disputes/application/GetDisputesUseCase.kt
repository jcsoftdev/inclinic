package com.inclinic.app.features.admin.disputes.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminDisputeItem
import kotlinx.coroutines.withContext

class GetDisputesUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(status: String? = null): Result<List<AdminDisputeItem>> =
        withContext(dispatchers.io) { dataSource.getDisputes(status) }
}
