package com.inclinic.app.features.patient.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.PatientProfile
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import kotlinx.coroutines.withContext

class UpdatePatientProfileUseCase(
    private val dataSource: PatientDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        patientId: String,
        name: String,
        phone: String?,
        dateOfBirth: String?,
    ): Result<PatientProfile> = withContext(dispatchers.io) {
        if (name.isBlank()) return@withContext Result.failure(IllegalArgumentException("Name cannot be blank"))
        dataSource.updatePatientProfile(patientId, name, phone, dateOfBirth)
    }
}
