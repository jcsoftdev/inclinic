package com.inclinic.app.features.admin.doctors.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class ApproveDoctorUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        withContext(dispatchers.io) { dataSource.approveDoctor(id) }
}
