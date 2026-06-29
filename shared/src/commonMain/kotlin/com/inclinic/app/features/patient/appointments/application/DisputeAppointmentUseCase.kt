package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class DisputeAppointmentUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        appointmentId: String,
        reason: String,
        details: String,
    ): Result<Unit> = withContext(dispatchers.io) {
        dataSource.disputeAppointment(appointmentId, reason, details)
    }
}
