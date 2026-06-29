package com.inclinic.app.features.patient.availability.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class GetAvailabilityUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String, date: String): Result<List<AvailabilitySlot>> =
        withContext(dispatchers.io) { dataSource.getAvailability(doctorId, date) }
}
