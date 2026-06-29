package com.inclinic.app.features.admin.subscriptions.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminSubscriptionsOverview
import kotlinx.coroutines.withContext

class GetSubscriptionsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<AdminSubscriptionsOverview> =
        withContext(dispatchers.io) { dataSource.getSubscriptions() }
}
