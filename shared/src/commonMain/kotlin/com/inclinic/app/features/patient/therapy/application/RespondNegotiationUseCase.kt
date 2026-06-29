package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.withContext

class RespondNegotiationUseCase(
    private val dataSource: TherapyPackageDataSource,
    private val dispatchers: AppDispatchers,
) {
    /** Returns the accepted therapyPackageId when [action] is ACCEPT, otherwise null. */
    suspend operator fun invoke(
        negotiationId: String,
        action: String,
        pricePerSession: Double?,
        sessions: Int?,
        message: String?,
    ): Result<String?> =
        withContext(dispatchers.io) { dataSource.respondNegotiation(negotiationId, action, pricePerSession, sessions, message) }
}
