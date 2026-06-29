package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.core.model.ShareStatus
import com.inclinic.app.features.patient.infrastructure.remote.ShareDataSource
import kotlinx.coroutines.withContext

class GetActiveAccessesUseCase(
    private val dataSource: ShareDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<ShareRequest>> =
        withContext(dispatchers.io) {
            dataSource.getShareRequests().map { list ->
                list.filter { it.status == ShareStatus.APPROVED }
            }
        }
}
