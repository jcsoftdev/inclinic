package com.inclinic.app.features.doctor.medical_records.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.MedicalRecord
import com.inclinic.app.core.model.MedicalRecordDraft
import com.inclinic.app.features.doctor.infrastructure.remote.CreateMedicalRecordRequest
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorMedicalRecordDataSource
import kotlinx.coroutines.withContext

class CreateMedicalRecordUseCase(
    private val dataSource: DoctorMedicalRecordDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(patientId: String, draft: MedicalRecordDraft): Result<MedicalRecord> =
        withContext(dispatchers.io) {
            if (draft.diagnosis.isBlank()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Diagnosis is required")
                )
            }
            dataSource.createMedicalRecord(
                CreateMedicalRecordRequest(
                    appointmentId = draft.appointmentId,
                    patientId = patientId,
                    diagnosis = draft.diagnosis,
                    symptoms = draft.symptoms,
                    treatment = draft.treatment,
                    prescription = draft.prescription.takeIf { it.isNotBlank() },
                    notes = draft.notes.takeIf { it.isNotBlank() },
                )
            )
        }
}
