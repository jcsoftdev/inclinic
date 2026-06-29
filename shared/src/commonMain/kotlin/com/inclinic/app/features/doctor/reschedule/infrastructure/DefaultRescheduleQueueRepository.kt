package com.inclinic.app.features.doctor.reschedule.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequest
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequestStatus
import com.inclinic.app.features.doctor.reschedule.core.port.RescheduleQueueRepository
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.RescheduleQueueDataSource
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RescheduleRequestDto
import com.inclinic.app.features.doctor.reschedule.infrastructure.remote.dto.RespondRescheduleRequestDto
import kotlinx.coroutines.withContext

class DefaultRescheduleQueueRepository(
    private val remote: RescheduleQueueDataSource,
    private val dispatchers: AppDispatchers,
) : RescheduleQueueRepository {

    override suspend fun listRequests(): Result<List<RescheduleRequest>> =
        withContext(dispatchers.io) { remote.listRequests().map { list -> list.map { it.toDomain() } } }

    override suspend fun respond(id: String, decision: RescheduleRequestStatus): Result<RescheduleRequest> =
        withContext(dispatchers.io) {
            remote.respond(id, RespondRescheduleRequestDto(decision.toAction()))
                .map { it.toDomain() }
        }

    private fun RescheduleRequestStatus.toAction(): String = when (this) {
        RescheduleRequestStatus.APPROVED -> "APPROVE"
        RescheduleRequestStatus.REJECTED -> "REJECT"
        else -> name
    }

    private fun RescheduleRequestDto.toDomain() = RescheduleRequest(
        id = id,
        patientName = patientName,
        currentSlot = currentSlot,
        requestedSlot = requestedSlot,
        reason = reason,
        status = runCatching { RescheduleRequestStatus.valueOf(status) }
            .getOrElse { RescheduleRequestStatus.UNKNOWN },
        dateLabel = dateLabel,
    )
}
