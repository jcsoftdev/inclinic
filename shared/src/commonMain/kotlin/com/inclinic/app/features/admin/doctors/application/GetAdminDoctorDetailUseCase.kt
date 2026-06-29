package com.inclinic.app.features.admin.doctors.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorDetail
import kotlinx.coroutines.withContext

class GetAdminDoctorDetailUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(id: String): Result<AdminDoctorDetail> =
        withContext(dispatchers.io) { dataSource.getDoctorDetail(id) }
}
