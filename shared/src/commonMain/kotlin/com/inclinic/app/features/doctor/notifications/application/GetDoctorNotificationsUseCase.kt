package com.inclinic.app.features.doctor.notifications.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.notifications.core.model.DoctorNotification
import com.inclinic.app.features.doctor.notifications.core.port.DoctorNotificationsRepository
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter
import kotlinx.coroutines.withContext

class GetDoctorNotificationsUseCase(
    private val repository: DoctorNotificationsRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(filter: NotificationFilter = NotificationFilter.ALL): Result<List<DoctorNotification>> =
        withContext(dispatchers.io) { repository.list(filter) }
}
