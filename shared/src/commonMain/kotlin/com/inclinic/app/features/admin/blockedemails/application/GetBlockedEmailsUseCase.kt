package com.inclinic.app.features.admin.blockedemails.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminBlockedEmailItem
import kotlinx.coroutines.withContext

class GetBlockedEmailsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<AdminBlockedEmailItem>> =
        withContext(dispatchers.io) {
            dataSource.getBlockedEmails()
        }
}
