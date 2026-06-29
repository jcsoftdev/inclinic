package com.inclinic.app.features.doctor.medical_records.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorMedicalRecordDataSource
import kotlinx.coroutines.withContext

class GetDoctorMedicalRecordsUseCase(
    private val dataSource: DoctorMedicalRecordDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(patientId: String): Result<List<MedicalRecord>> =
        withContext(dispatchers.io) { dataSource.getMedicalRecords(patientId) }
}
