package com.inclinic.app.features.admin.finance.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

/**
 * Triggers a CSV export of finance data via GET /api/admin/finance/export?format=csv.
 * Returns raw CSV bytes. Saving/sharing the bytes is a platform-level concern.
 * Requires SUPER_ADMIN role.
 */
class ExportFinanceCsvUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<ByteArray> =
        withContext(dispatchers.io) { dataSource.exportFinanceCsv() }
}
