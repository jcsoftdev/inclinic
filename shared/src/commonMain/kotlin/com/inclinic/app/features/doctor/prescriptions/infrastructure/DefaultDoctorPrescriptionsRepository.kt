package com.inclinic.app.features.doctor.prescriptions.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.prescriptions.core.model.Prescription
import com.inclinic.app.features.doctor.prescriptions.core.model.PrescriptionItem
import com.inclinic.app.features.doctor.prescriptions.core.model.UpdatePrescriptionDraft
import com.inclinic.app.features.doctor.prescriptions.core.port.DoctorPrescriptionsRepository
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.DoctorPrescriptionsDataSource
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.PrescriptionDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.UpdatePrescriptionItemDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.UpdatePrescriptionRequestDto
import kotlinx.coroutines.withContext

class DefaultDoctorPrescriptionsRepository(
    private val remote: DoctorPrescriptionsDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorPrescriptionsRepository {

    override suspend fun getPrescription(id: String): Result<Prescription> =
        withContext(dispatchers.io) {
            remote.getPrescription(id).map(::toDomain)
        }

    override suspend fun updatePrescription(id: String, draft: UpdatePrescriptionDraft): Result<Prescription> =
        withContext(dispatchers.io) {
            val body = UpdatePrescriptionRequestDto(
                diagnosis = draft.diagnosis,
                instructions = draft.instructions,
                notes = draft.notes,
                validUntil = draft.validUntil,
                items = draft.items.map { item ->
                    UpdatePrescriptionItemDto(
                        medicationName = item.medicationName,
                        dosage = item.dosage,
                        frequency = item.frequency,
                        duration = item.duration,
                        notes = item.notes,
                        order = item.order,
                    )
                },
            )
            remote.updatePrescription(id, body).map(::toDomain)
        }

    private fun toDomain(dto: PrescriptionDto) = Prescription(
        id = dto.id,
        appointmentId = dto.appointmentId,
        doctorId = dto.doctorId,
        patientId = dto.patientId,
        diagnosis = dto.diagnosis,
        instructions = dto.instructions,
        notes = dto.notes,
        validUntil = dto.validUntil,
        doctorFullName = dto.doctorFullName,
        doctorSignature = dto.doctorSignature,
        createdAt = dto.createdAt,
        items = dto.items.map { item ->
            PrescriptionItem(
                id = item.id,
                medicationName = item.medicationName,
                dosage = item.dosage,
                frequency = item.frequency,
                duration = item.duration,
                notes = item.notes,
                order = item.order,
            )
        },
    )
}
