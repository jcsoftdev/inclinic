package com.inclinic.app.features.doctor.prescriptions.infrastructure.remote

import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.CreatePrescriptionRequestDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.PrescriptionDto
import com.inclinic.app.features.doctor.prescriptions.infrastructure.remote.dto.UpdatePrescriptionRequestDto

interface DoctorPrescriptionsDataSource {
    suspend fun getPrescription(id: String): Result<PrescriptionDto>
    suspend fun createPrescription(body: CreatePrescriptionRequestDto): Result<PrescriptionDto>
    suspend fun updatePrescription(id: String, body: UpdatePrescriptionRequestDto): Result<PrescriptionDto>
}
