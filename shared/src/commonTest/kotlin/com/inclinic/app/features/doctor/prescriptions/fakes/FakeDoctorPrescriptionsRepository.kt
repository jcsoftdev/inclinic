package com.inclinic.app.features.doctor.prescriptions.fakes

import com.inclinic.app.features.doctor.prescriptions.core.model.Prescription
import com.inclinic.app.features.doctor.prescriptions.core.model.PrescriptionItem
import com.inclinic.app.features.doctor.prescriptions.core.model.UpdatePrescriptionDraft
import com.inclinic.app.features.doctor.prescriptions.core.port.DoctorPrescriptionsRepository

class FakeDoctorPrescriptionsRepository : DoctorPrescriptionsRepository {
    var getResult: Result<Prescription> = Result.success(prescriptionFixture())
    var updateResult: Result<Prescription> = Result.success(prescriptionFixture())
    var lastUpdatedId: String? = null
    var lastUpdatedDraft: UpdatePrescriptionDraft? = null

    override suspend fun getPrescription(id: String): Result<Prescription> = getResult

    override suspend fun updatePrescription(id: String, draft: UpdatePrescriptionDraft): Result<Prescription> {
        lastUpdatedId = id
        lastUpdatedDraft = draft
        return updateResult
    }
}

fun prescriptionFixture(
    id: String = "rx-1",
    appointmentId: String = "apt-1",
    medication: String = "Losartan 50 mg",
) = Prescription(
    id = id,
    appointmentId = appointmentId,
    doctorId = "doc-1",
    patientId = "pat-1",
    diagnosis = null,
    instructions = "Tomar con agua",
    notes = null,
    validUntil = null,
    doctorFullName = "Dr. Lopez",
    doctorSignature = "RX-SIG-01",
    createdAt = "2026-06-01T10:00:00Z",
    items = listOf(
        PrescriptionItem(
            id = "item-1",
            medicationName = medication,
            dosage = "50mg",
            frequency = "Cada 12h",
            duration = "30 dias",
            notes = null,
            order = 0,
        ),
    ),
)
