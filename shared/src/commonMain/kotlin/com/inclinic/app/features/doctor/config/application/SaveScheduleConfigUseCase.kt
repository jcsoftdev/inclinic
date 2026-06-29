package com.inclinic.app.features.doctor.config.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorScheduleDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.WeeklySchedule
import kotlinx.coroutines.withContext

class SaveScheduleConfigUseCase(
    private val dataSource: DoctorScheduleDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String, schedule: WeeklySchedule): Result<WeeklySchedule> =
        withContext(dispatchers.io) {
            val invalid = schedule.days.firstOrNull { it.startTime >= it.endTime }
            if (invalid != null) {
                return@withContext Result.failure(
                    IllegalArgumentException(
                        "Hora de fin debe ser posterior a la de inicio (${invalid.dayOfWeek})"
                    )
                )
            }
            dataSource.saveWeeklySchedule(doctorId, schedule)
        }
}
