package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.RescheduleProposal
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class GetRescheduleProposalUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(appointmentId: String): Result<RescheduleProposal?> =
        withContext(dispatchers.io) {
            dataSource.getPendingRescheduleProposal(appointmentId)
        }
}
