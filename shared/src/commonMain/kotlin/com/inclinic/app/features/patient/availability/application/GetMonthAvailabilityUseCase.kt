package com.inclinic.app.features.patient.availability.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.AppointmentDataSource
import kotlinx.coroutines.withContext

class GetMonthAvailabilityUseCase(
    private val dataSource: AppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String, month: String): Result<Map<String, String>> =
        withContext(dispatchers.io) { dataSource.getMonthAvailability(doctorId, month) }
}
