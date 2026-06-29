package com.inclinic.app.features.doctor.negotiation.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.negotiation.core.model.NegotiationAction
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiation
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiationStatus
import com.inclinic.app.features.doctor.negotiation.core.port.DoctorNegotiationRepository
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.DoctorNegotiationDataSource
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.dto.PackageNegotiationDto
import com.inclinic.app.features.doctor.negotiation.infrastructure.remote.dto.RespondNegotiationDto
import kotlinx.coroutines.withContext

class DefaultDoctorNegotiationRepository(
    private val remote: DoctorNegotiationDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorNegotiationRepository {

    override suspend fun getNegotiation(id: String): Result<PackageNegotiation> =
        withContext(dispatchers.io) { remote.getNegotiation(id).map { it.toDomain() } }

    override suspend fun respondNegotiation(
        id: String,
        action: NegotiationAction,
        counterPriceCents: Int?,
    ): Result<PackageNegotiation> =
        withContext(dispatchers.io) {
            remote.respondNegotiation(id, RespondNegotiationDto(action.name, counterPriceCents))
                .map { it.toDomain() }
        }

    private fun PackageNegotiationDto.toDomain() = PackageNegotiation(
        id = id,
        patientName = patientName,
        packageName = packageName,
        originalPriceCents = originalPriceCents,
        proposedPriceCents = proposedPriceCents,
        message = message,
        status = runCatching { PackageNegotiationStatus.valueOf(status) }
            .getOrElse { PackageNegotiationStatus.UNKNOWN },
    )
}
