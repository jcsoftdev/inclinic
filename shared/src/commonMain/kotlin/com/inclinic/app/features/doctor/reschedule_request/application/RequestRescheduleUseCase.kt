package com.inclinic.app.features.doctor.reschedule_request.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.doctor.reschedule_request.core.port.RescheduleRequestRepository
import kotlinx.coroutines.withContext

class RequestRescheduleUseCase(
    private val repository: RescheduleRequestRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        appointmentId: String,
        proposedSlot: String,
        message: String?,
    ): Result<Appointment> =
        withContext(dispatchers.io) { repository.requestReschedule(appointmentId, proposedSlot, message) }
}
