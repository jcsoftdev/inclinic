package com.inclinic.app.features.doctor.schedule.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import kotlinx.coroutines.withContext

class GetDailyScheduleUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String, date: String): Result<List<Appointment>> =
        withContext(dispatchers.io) { dataSource.getDailySchedule(doctorId, date) }
}
