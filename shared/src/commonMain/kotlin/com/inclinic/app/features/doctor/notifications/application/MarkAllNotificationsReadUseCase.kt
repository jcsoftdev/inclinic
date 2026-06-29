package com.inclinic.app.features.doctor.notifications.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.notifications.core.port.DoctorNotificationsRepository
import kotlinx.coroutines.withContext

class MarkAllNotificationsReadUseCase(
    private val repository: DoctorNotificationsRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<Unit> =
        withContext(dispatchers.io) { repository.markAllRead() }
}
