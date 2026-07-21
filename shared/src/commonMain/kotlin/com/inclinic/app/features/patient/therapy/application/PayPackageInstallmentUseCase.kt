package com.inclinic.app.features.patient.therapy.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.infrastructure.remote.TherapyPackageDataSource
import kotlinx.coroutines.withContext

/**
 * Registra un abono parcial del paquete. Tras un abono el backend recalcula el
 * precio (la erosión del descuento puede subir el total), así que el llamador
 * debe recargar el estado de cuenta después de invocar esto.
 */
class PayPackageInstallmentUseCase(
    private val dataSource: TherapyPackageDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(packageId: String, amount: Double): Result<Unit> =
        withContext(dispatchers.io) { dataSource.payPackageInstallment(packageId, amount) }
}
