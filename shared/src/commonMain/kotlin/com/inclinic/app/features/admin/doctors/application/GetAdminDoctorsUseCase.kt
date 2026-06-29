package com.inclinic.app.features.admin.doctors.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorListItem
import kotlinx.coroutines.withContext

class GetAdminDoctorsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(status: String? = null, q: String? = null): Result<List<AdminDoctorListItem>> =
        withContext(dispatchers.io) { dataSource.getDoctors(status, q) }
}
