package com.inclinic.app.features.admin.patients.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import kotlinx.coroutines.withContext

class GetPatientsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(status: String? = null, q: String? = null): Result<List<AdminPatientListItem>> =
        withContext(dispatchers.io) { dataSource.getPatients(status, q) }
}
