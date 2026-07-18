package com.inclinic.app.features.doctor.prescriptions.core.port

import com.inclinic.app.features.doctor.prescriptions.core.model.CreatePrescriptionDraft
import com.inclinic.app.features.doctor.prescriptions.core.model.Prescription
import com.inclinic.app.features.doctor.prescriptions.core.model.UpdatePrescriptionDraft

interface DoctorPrescriptionsRepository {
    suspend fun getPrescription(id: String): Result<Prescription>
    suspend fun createPrescription(draft: CreatePrescriptionDraft): Result<Prescription>
    suspend fun updatePrescription(id: String, draft: UpdatePrescriptionDraft): Result<Prescription>
}
