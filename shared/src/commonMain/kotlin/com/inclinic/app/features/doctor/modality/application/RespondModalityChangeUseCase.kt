package com.inclinic.app.features.doctor.modality.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.modality.core.model.ModalityChangeRequest
import com.inclinic.app.features.doctor.modality.core.model.ModalityResponseAction
import com.inclinic.app.features.doctor.modality.core.port.ModalityRequestRepository
import kotlinx.coroutines.withContext

class RespondModalityChangeUseCase(
    private val repository: ModalityRequestRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        id: String,
        action: ModalityResponseAction,
        adjustedPrice: Int? = null,
    ): Result<ModalityChangeRequest> =
        withContext(dispatchers.io) { repository.respond(id, action, adjustedPrice) }
}
