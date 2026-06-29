package com.inclinic.app.features.doctor.packages.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage
import com.inclinic.app.features.doctor.packages.core.port.DoctorPackagesRepository
import kotlinx.coroutines.withContext

class GetDoctorPackagesUseCase(
    private val repository: DoctorPackagesRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<TherapyPackage>> =
        withContext(dispatchers.io) { repository.list() }
}
