package com.inclinic.app.features.admin.appointments.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentFilters
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class GetAdminAppointmentsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(filters: AdminAppointmentFilters = AdminAppointmentFilters()): Result<List<AdminAppointmentListItem>> =
        withContext(dispatchers.io) { dataSource.getAppointments(filters) }
}
