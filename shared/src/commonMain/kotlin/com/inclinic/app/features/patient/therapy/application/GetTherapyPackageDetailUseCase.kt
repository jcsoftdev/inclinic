package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.withContext

class GetTherapyPackageDetailUseCase(
    private val dataSource: TherapyPackageDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(packageId: String): Result<Pair<TherapyPackage, List<PackageSession>>> =
        withContext(dispatchers.io) { dataSource.getPackageDetail(packageId) }
}
