package com.inclinic.app.features.doctor.reschedule_request.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext

/** Slots libres del propio médico en una fecha, para proponer una reagenda con horarios reales. */
class GetDoctorAvailabilityUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String, date: String): Result<List<AvailabilitySlot>> =
        withContext(dispatchers.io) { dataSource.getAvailability(doctorId, date) }
}
