package com.inclinic.app.features.doctor.modality.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.modality.core.model.ModalityChangeRequest
import com.inclinic.app.features.doctor.modality.core.model.ModalityRequestStatus
import com.inclinic.app.features.doctor.modality.core.model.ModalityResponseAction
import com.inclinic.app.features.doctor.modality.core.port.ModalityRequestRepository
import com.inclinic.app.features.doctor.modality.infrastructure.remote.ModalityRequestDataSource
import com.inclinic.app.features.doctor.modality.infrastructure.remote.dto.ModalityChangeRequestDto
import com.inclinic.app.features.doctor.modality.infrastructure.remote.dto.RespondModalityChangeDto
import kotlinx.coroutines.withContext

class DefaultModalityRequestRepository(
    private val remote: ModalityRequestDataSource,
    private val dispatchers: AppDispatchers,
) : ModalityRequestRepository {

    override suspend fun getRequest(id: String): Result<ModalityChangeRequest> =
        withContext(dispatchers.io) { remote.getRequest(id).map { it.toDomain() } }

    override suspend fun respond(
        id: String,
        action: ModalityResponseAction,
        adjustedPrice: Int?,
    ): Result<ModalityChangeRequest> =
        withContext(dispatchers.io) {
            remote.respond(
                id,
                RespondModalityChangeDto(action = action.name, adjustedPrice = adjustedPrice),
            ).map { it.toDomain() }
        }

    private fun ModalityChangeRequestDto.toDomain() = ModalityChangeRequest(
        id = id,
        patientName = patientName,
        patientSubtitle = patientSubtitle,
        appointmentSlot = appointmentSlot,
        currentModality = currentModality,
        requestedModality = requestedModality,
        reason = reason,
        address = address,
        suggestedPrice = suggestedPrice,
        status = runCatching { ModalityRequestStatus.valueOf(status) }.getOrElse { ModalityRequestStatus.UNKNOWN },
    )
}
