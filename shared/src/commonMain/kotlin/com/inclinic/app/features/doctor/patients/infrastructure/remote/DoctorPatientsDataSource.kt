package com.inclinic.app.features.doctor.patients.infrastructure.remote

import com.inclinic.app.features.doctor.patients.infrastructure.remote.dto.PatientListDto
import com.inclinic.app.features.doctor.patients.infrastructure.remote.dto.PatientListItemDto

interface DoctorPatientsDataSource {
    suspend fun getPatients(): Result<PatientListDto>
    suspend fun searchPatientByEmail(query: String): Result<List<PatientListItemDto>>
}
