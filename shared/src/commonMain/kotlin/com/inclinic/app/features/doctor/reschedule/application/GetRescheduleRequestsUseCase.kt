package com.inclinic.app.features.doctor.reschedule.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequest
import com.inclinic.app.features.doctor.reschedule.core.port.RescheduleQueueRepository
import kotlinx.coroutines.withContext

class GetRescheduleRequestsUseCase(
    private val repository: RescheduleQueueRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<RescheduleRequest>> =
        withContext(dispatchers.io) { repository.listRequests() }
}
