package com.inclinic.app.features.patient.doctor_profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.features.patient.infrastructure.remote.DoctorSearchDataSource
import kotlinx.coroutines.withContext

class GetDoctorDetailUseCase(
    private val dataSource: DoctorSearchDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(doctorId: String): Result<Doctor> =
        withContext(dispatchers.io) { dataSource.getDoctorById(doctorId) }
}
