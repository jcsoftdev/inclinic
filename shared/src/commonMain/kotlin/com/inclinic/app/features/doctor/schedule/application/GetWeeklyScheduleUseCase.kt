package com.inclinic.app.features.doctor.schedule.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext

class GetWeeklyScheduleUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String, weekStart: String): Result<List<DaySummary>> =
        withContext(dispatchers.io) { dataSource.getWeeklySchedule(doctorId, weekStart) }
}
