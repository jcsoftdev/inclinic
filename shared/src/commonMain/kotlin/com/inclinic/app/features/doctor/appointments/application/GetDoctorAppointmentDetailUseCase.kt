package com.inclinic.app.features.doctor.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext

class GetDoctorAppointmentDetailUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(appointmentId: String): Result<Appointment> =
        withContext(dispatchers.io) { dataSource.getAppointmentById(appointmentId) }
}
