package com.inclinic.app.features.doctor.patients.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.patients.core.model.PatientList
import com.inclinic.app.features.doctor.patients.core.port.DoctorPatientsRepository
import kotlinx.coroutines.withContext

class GetDoctorPatientsUseCase(
    private val repository: DoctorPatientsRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<PatientList> =
        withContext(dispatchers.io) { repository.getPatients() }
}
