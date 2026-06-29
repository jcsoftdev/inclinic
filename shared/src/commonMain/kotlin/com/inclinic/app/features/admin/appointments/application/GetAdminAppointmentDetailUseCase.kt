package com.inclinic.app.features.admin.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class GetAdminAppointmentDetailUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(id: String): Result<AdminAppointmentDetail> =
        withContext(dispatchers.io) { dataSource.getAppointmentDetail(id) }
}
