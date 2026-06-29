package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class RescheduleAppointmentUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        appointmentId: String,
        date: String,
        slotId: String,
    ): Result<Appointment> = withContext(dispatchers.io) {
        dataSource.rescheduleAppointment(appointmentId, date, slotId)
    }
}
