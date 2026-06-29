package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.MedicalRecordDetail
import com.inclinic.app.features.patient.infrastructure.remote.MedicalRecordDataSource
import kotlinx.coroutines.withContext

class GetMedicalRecordDetailUseCase(
    private val dataSource: MedicalRecordDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(recordId: String): Result<MedicalRecordDetail> =
        withContext(dispatchers.io) { dataSource.getRecordDetail(recordId) }
}
