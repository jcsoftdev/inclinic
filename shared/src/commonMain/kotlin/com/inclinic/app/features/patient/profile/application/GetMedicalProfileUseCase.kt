package com.inclinic.app.features.patient.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.MedicalProfile
import com.inclinic.app.features.patient.infrastructure.remote.PatientDataSource
import kotlinx.coroutines.withContext

class GetMedicalProfileUseCase(
    private val dataSource: PatientDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(patientId: String): Result<MedicalProfile> =
        withContext(dispatchers.io) { dataSource.getMedicalProfile(patientId) }
}
