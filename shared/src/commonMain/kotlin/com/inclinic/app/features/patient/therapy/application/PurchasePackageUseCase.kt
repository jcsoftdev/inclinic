package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.withContext

class PurchasePackageUseCase(
    private val dataSource: TherapyPackageDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(offerId: String): Result<String> =
        withContext(dispatchers.io) { dataSource.purchasePackage(offerId) }
}
