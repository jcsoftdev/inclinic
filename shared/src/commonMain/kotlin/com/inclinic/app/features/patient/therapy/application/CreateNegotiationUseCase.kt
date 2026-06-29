package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.PackageNegotiation
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.withContext

class CreateNegotiationUseCase(
    private val dataSource: TherapyPackageDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        offerId: String,
        pricePerSession: Double,
        sessions: Int,
        message: String?,
    ): Result<PackageNegotiation> =
        withContext(dispatchers.io) { dataSource.createNegotiation(offerId, pricePerSession, sessions, message) }
}
