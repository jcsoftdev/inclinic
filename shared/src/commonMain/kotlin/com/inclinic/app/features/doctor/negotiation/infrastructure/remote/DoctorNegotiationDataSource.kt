package com.inclinic.app.features.doctor.negotiation.infrastructure.remote

import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.dto.PackageNegotiationDto
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.dto.RespondNegotiationDto

interface DoctorNegotiationDataSource {
    suspend fun getNegotiation(id: String): Result<PackageNegotiationDto>
    suspend fun respondNegotiation(id: String, body: RespondNegotiationDto): Result<PackageNegotiationDto>
}
