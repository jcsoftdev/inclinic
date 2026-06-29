package com.inclinic.app.features.doctor.patients.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.core.port.DoctorPatientsRepository
import kotlinx.coroutines.withContext

class SearchPatientByEmailUseCase(
    private val repository: DoctorPatientsRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(query: String): Result<List<PatientListItem>> =
        withContext(dispatchers.io) { repository.searchPatientByEmail(query) }
}
