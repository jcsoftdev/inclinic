package com.inclinic.app.features.doctor.packages.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.packages.core.port.DoctorPackagesRepository
import kotlinx.coroutines.withContext

class CancelPackageUseCase(
    private val repository: DoctorPackagesRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        withContext(dispatchers.io) { repository.cancel(id) }
}
