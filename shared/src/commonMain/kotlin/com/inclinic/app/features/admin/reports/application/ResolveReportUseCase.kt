package com.inclinic.app.features.admin.reports.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class ResolveReportUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        reportId: String,
        status: String,
        adminNote: String? = null,
    ): Result<Unit> =
        withContext(dispatchers.io) { dataSource.resolveReport(reportId, status, adminNote) }
}
