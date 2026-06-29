package com.inclinic.app.features.doctor.patients.core.port

import com.inclinic.app.features.doctor.patients.core.model.PatientList
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem

interface DoctorPatientsRepository {
    suspend fun getPatients(): Result<PatientList>
    suspend fun searchPatientByEmail(query: String): Result<List<PatientListItem>>
}
