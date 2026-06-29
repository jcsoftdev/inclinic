package com.inclinic.app.features.doctor.config.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorScheduleDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.WeeklySchedule
import kotlinx.coroutines.withContext

class GetScheduleConfigUseCase(
    private val dataSource: DoctorScheduleDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String): Result<WeeklySchedule> =
        withContext(dispatchers.io) { dataSource.getWeeklySchedule(doctorId) }
}
