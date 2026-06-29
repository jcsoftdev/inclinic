package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class RequestVisitTypeChangeUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        appointmentId: String,
        newVisitType: String,
        reason: String?,
        address: String?,
    ): Result<Unit> = withContext(dispatchers.io) {
        dataSource.requestVisitTypeChange(appointmentId, newVisitType, address, reason)
    }
}
