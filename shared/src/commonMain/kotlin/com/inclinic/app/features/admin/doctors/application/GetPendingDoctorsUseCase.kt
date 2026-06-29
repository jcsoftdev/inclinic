package com.inclinic.app.features.admin.doctors.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminPendingDoctor
import kotlinx.coroutines.withContext

class GetPendingDoctorsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<AdminPendingDoctor>> =
        withContext(dispatchers.io) { dataSource.getPendingDoctors() }
}
