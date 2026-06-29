package com.inclinic.app.features.doctor.modality.core.port

import com.inclinic.app.features.doctor.modality.core.model.ModalityChangeRequest
import com.inclinic.app.features.doctor.modality.core.model.ModalityResponseAction

interface ModalityRequestRepository {
    suspend fun getRequest(id: String): Result<ModalityChangeRequest>
    suspend fun respond(
        id: String,
        action: ModalityResponseAction,
        adjustedPrice: Int? = null,
    ): Result<ModalityChangeRequest>
}
