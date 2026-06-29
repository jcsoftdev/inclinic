package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.features.patient.infrastructure.remote.MedicalRecordDataSource
import kotlinx.coroutines.withContext

class GetMedicalHistoryUseCase(
    private val dataSource: MedicalRecordDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(patientId: String): Result<List<MedicalRecord>> =
        withContext(dispatchers.io) { dataSource.getPatientRecords(patientId) }
}
