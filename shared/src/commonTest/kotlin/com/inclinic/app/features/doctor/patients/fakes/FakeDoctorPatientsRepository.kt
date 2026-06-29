package com.inclinic.app.features.doctor.patients.fakes

import com.inclinic.app.features.doctor.patients.core.model.PatientList
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.core.port.DoctorPatientsRepository

class FakeDoctorPatientsRepository : DoctorPatientsRepository {

    var getPatientsResult: Result<PatientList> = Result.success(PatientList())
    var searchResult: Result<List<PatientListItem>> = Result.success(emptyList())

    var getPatientsCallCount = 0
    var searchCallCount = 0
    var lastSearchQuery: String? = null

    override suspend fun getPatients(): Result<PatientList> {
        getPatientsCallCount++
        return getPatientsResult
    }

    override suspend fun searchPatientByEmail(query: String): Result<List<PatientListItem>> {
        searchCallCount++
        lastSearchQuery = query
        return searchResult
    }
}
