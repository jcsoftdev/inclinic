package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Prescription
import com.inclinic.app.features.patient.infrastructure.remote.PrescriptionDataSource
import kotlinx.coroutines.withContext

class GetPrescriptionDetailUseCase(
    private val dataSource: PrescriptionDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(prescriptionId: String): Result<Prescription> =
        withContext(dispatchers.io) { dataSource.getPrescriptionDetail(prescriptionId) }
}
