package com.inclinic.app.features.doctor.dashboard.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorDashboard
import kotlinx.coroutines.withContext

class GetDoctorDashboardUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String): Result<DoctorDashboard> =
        withContext(dispatchers.io) { dataSource.getDashboard(doctorId) }
}
