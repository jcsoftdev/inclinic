package com.inclinic.app.features.patient.notifications.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.AppNotification
import com.inclinic.app.features.patient.infrastructure.remote.NotificationDataSource
import kotlinx.coroutines.withContext

class GetNotificationsUseCase(
    private val dataSource: NotificationDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<AppNotification>> =
        withContext(dispatchers.io) { dataSource.getNotifications() }
}
