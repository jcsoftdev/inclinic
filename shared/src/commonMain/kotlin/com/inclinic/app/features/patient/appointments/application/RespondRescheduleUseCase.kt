package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class RespondRescheduleUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        requestId: String,
        accept: Boolean,
        responseNote: String? = null,
    ): Result<Unit> = withContext(dispatchers.io) {
        dataSource.respondRescheduleProposal(requestId, accept, responseNote)
    }
}
