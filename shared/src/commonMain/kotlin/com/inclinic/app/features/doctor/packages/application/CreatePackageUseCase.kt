package com.inclinic.app.features.doctor.packages.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.packages.core.model.TherapyPackage
import com.inclinic.app.features.doctor.packages.core.port.DoctorPackagesRepository
import com.inclinic.app.features.doctor.packages.core.port.NewPackageDraft
import kotlinx.coroutines.withContext

class CreatePackageUseCase(
    private val repository: DoctorPackagesRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(draft: NewPackageDraft): Result<TherapyPackage> =
        withContext(dispatchers.io) { repository.create(draft) }
}
