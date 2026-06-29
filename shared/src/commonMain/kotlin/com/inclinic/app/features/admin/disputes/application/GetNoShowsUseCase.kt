package com.inclinic.app.features.admin.disputes.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminNoShowItem
import kotlinx.coroutines.withContext

class GetNoShowsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<AdminNoShowItem>> =
        withContext(dispatchers.io) { dataSource.getNoShows() }
}
