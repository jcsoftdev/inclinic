package com.inclinic.app.features.doctor.modality.infrastructure.remote

import com.inclinic.app.features.doctor.modality.infrastructure.remote.dto.ModalityChangeRequestDto
import com.inclinic.app.features.doctor.modality.infrastructure.remote.dto.RespondModalityChangeDto

interface ModalityRequestDataSource {
    suspend fun getRequest(id: String): Result<ModalityChangeRequestDto>
    suspend fun respond(id: String, body: RespondModalityChangeDto): Result<ModalityChangeRequestDto>
}
