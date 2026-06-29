package com.inclinic.app.features.admin.reports.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminReportItem
import kotlinx.coroutines.withContext

class GetReportsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(status: String? = null): Result<List<AdminReportItem>> =
        withContext(dispatchers.io) { dataSource.getReports(status) }
}
