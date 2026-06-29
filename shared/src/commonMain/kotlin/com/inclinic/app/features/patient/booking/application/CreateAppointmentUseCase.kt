package com.inclinic.app.features.patient.booking.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class CreateAppointmentUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        doctorId: String,
        date: String,
        slotId: String,
        visitType: String,
        notes: String?,
    ): Result<Appointment> = withContext(dispatchers.io) {
        if (visitType.isBlank()) return@withContext Result.failure(IllegalArgumentException("Visit type is required"))
        dataSource.createAppointment(doctorId, date, slotId, visitType, notes)
    }
}
