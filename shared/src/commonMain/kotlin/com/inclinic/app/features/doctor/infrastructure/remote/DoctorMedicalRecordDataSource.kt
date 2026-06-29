package com.inclinic.app.features.doctor.infrastructure.remote

import com.inclinic.app.core.model.MedicalRecord

interface DoctorMedicalRecordDataSource {
    suspend fun getMedicalRecords(patientId: String): Result<List<MedicalRecord>>
    suspend fun getMedicalRecordById(recordId: String): Result<MedicalRecord>
    suspend fun createMedicalRecord(request: CreateMedicalRecordRequest): Result<MedicalRecord>
    suspend fun updateMedicalRecord(recordId: String, request: UpdateMedicalRecordRequest): Result<MedicalRecord>
}

data class CreateMedicalRecordRequest(
    val appointmentId: String?,
    val patientId: String,
    val diagnosis: String,
    val symptoms: String,
    val treatment: String,
    val prescription: String?,
    val notes: String?,
)

data class UpdateMedicalRecordRequest(
    val diagnosis: String?,
    val symptoms: String?,
    val treatment: String?,
    val prescription: String?,
    val notes: String?,
)
