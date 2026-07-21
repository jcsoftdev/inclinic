package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.PackageStatement
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.withContext

/** Estado de cuenta del paquete: saldo vigente + coste de seguir fraccionando. */
class GetPackageStatementUseCase(
    private val dataSource: TherapyPackageDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(packageId: String): Result<PackageStatement> =
        withContext(dispatchers.io) { dataSource.getPackageStatement(packageId) }
}
