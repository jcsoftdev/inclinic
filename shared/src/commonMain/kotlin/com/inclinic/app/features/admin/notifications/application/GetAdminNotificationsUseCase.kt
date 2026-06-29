package com.inclinic.app.features.admin.notifications.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.notifications.core.model.AdminNotification
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationsRepository
import kotlinx.coroutines.withContext

class GetAdminNotificationsUseCase(
    private val repository: AdminNotificationsRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(limit: Int = 30): Result<List<AdminNotification>> =
        withContext(dispatchers.io) { repository.list(limit) }
}
