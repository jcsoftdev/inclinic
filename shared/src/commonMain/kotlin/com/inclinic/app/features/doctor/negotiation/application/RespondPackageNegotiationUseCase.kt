package com.inclinic.app.features.doctor.negotiation.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.negotiation.core.model.NegotiationAction
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiation
import com.inclinic.app.features.doctor.negotiation.core.port.DoctorNegotiationRepository
import kotlinx.coroutines.withContext

class RespondPackageNegotiationUseCase(
    private val repository: DoctorNegotiationRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        id: String,
        action: NegotiationAction,
        counterPriceCents: Int?,
    ): Result<PackageNegotiation> =
        withContext(dispatchers.io) { repository.respondNegotiation(id, action, counterPriceCents) }
}
