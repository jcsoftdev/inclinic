package com.inclinic.app.features.patient.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class GetPatientAppointmentsUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(patientId: String, status: String?, page: Int): Result<List<Appointment>> =
        withContext(dispatchers.io) { dataSource.getPatientAppointments(patientId, status, page) }
}
