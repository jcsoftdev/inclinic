package com.inclinic.app.features.admin.notifications.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationsRepository
import kotlinx.coroutines.withContext

class MarkAllAdminNotificationsReadUseCase(
    private val repository: AdminNotificationsRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<Unit> =
        withContext(dispatchers.io) { repository.markAllRead() }
}
