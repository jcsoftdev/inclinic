package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.features.patient.infrastructure.remote.ShareDataSource
import kotlinx.coroutines.withContext

class RespondShareRequestUseCase(
    private val dataSource: ShareDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(requestId: String, approved: Boolean, durationDays: Int?): Result<ShareRequest> =
        withContext(dispatchers.io) {
            val action = if (approved) "approve" else "reject"
            dataSource.respondToShareRequest(requestId, action, durationDays)
        }
}
