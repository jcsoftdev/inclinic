package com.inclinic.app.features.doctor.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext

class ConfirmAppointmentUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(appointment: Appointment): Result<Appointment> =
        withContext(dispatchers.io) {
            if (appointment.status != AppointmentStatus.SCHEDULED &&
                appointment.status != AppointmentStatus.PENDING_PAYMENT
            ) {
                return@withContext Result.failure(
                    IllegalStateException("Appointment must be in SCHEDULED status to confirm")
                )
            }
            dataSource.confirmAppointment(appointment.id)
        }
}
