package com.inclinic.app.features.patient.home.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import com.inclinic.app.features.patient.infrastructure.remote.PatientDashboard
import kotlinx.coroutines.withContext

class GetPatientDashboardUseCase(
    private val dataSource: PatientDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(patientId: String): Result<PatientDashboard> =
        withContext(dispatchers.io) { dataSource.getDashboard(patientId) }
}
