package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.withContext

class GetTherapyPackagesUseCase(
    private val dataSource: TherapyPackageDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(patientId: String, status: String? = null): Result<List<TherapyPackage>> =
        withContext(dispatchers.io) { dataSource.getPatientPackages(patientId, status) }
}
