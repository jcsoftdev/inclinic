package com.inclinic.app.features.patient.notifications.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.NotificationDataSource
import kotlinx.coroutines.withContext

class MarkAllNotificationsReadUseCase(
    private val dataSource: NotificationDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<Unit> =
        withContext(dispatchers.io) { dataSource.markAllRead() }
}
