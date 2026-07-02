package com.inclinic.app.features.doctor.pending_closure.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.infrastructure.remote.DoctorAppointmentDataSource
import com.inclinic.app.features.doctor.pending_closure.core.model.PendingClosureItem
import kotlinx.coroutines.withContext

class GetPendingClosureQueueUseCase(
    private val dataSource: DoctorAppointmentDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        from: String? = null,
        to: String? = null,
    ): Result<List<PendingClosureItem>> =
        withContext(dispatchers.io) {
            dataSource.getPendingClosureAppointments(from, to)
        }
}
