package com.inclinic.app.features.doctor.reschedule_request.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.doctor.reschedule_request.core.port.RescheduleRequestRepository
import com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote.RescheduleRequestDataSource
import com.inclinic.app.features.doctor.reschedule_request.infrastructure.remote.dto.CreateRescheduleRequestDto
import kotlinx.coroutines.withContext

class DefaultRescheduleRequestRepository(
    private val remote: RescheduleRequestDataSource,
    private val dispatchers: AppDispatchers,
) : RescheduleRequestRepository {

    override suspend fun requestReschedule(
        appointmentId: String,
        proposedSlot: String,
        message: String?,
    ): Result<Appointment> =
        withContext(dispatchers.io) {
            remote.requestReschedule(appointmentId, CreateRescheduleRequestDto(proposedSlot, message))
        }
}
