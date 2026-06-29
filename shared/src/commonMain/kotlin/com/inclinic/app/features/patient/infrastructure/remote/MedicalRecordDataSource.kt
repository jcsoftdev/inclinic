package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.HistoryAccessLog
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.core.model.MedicalRecordDetail

interface MedicalRecordDataSource {
    suspend fun getPatientRecords(patientId: String): Result<List<MedicalRecord>>
    suspend fun getRecordDetail(recordId: String): Result<MedicalRecordDetail>
    suspend fun getAccessLogs(): Result<List<HistoryAccessLog>>
}
