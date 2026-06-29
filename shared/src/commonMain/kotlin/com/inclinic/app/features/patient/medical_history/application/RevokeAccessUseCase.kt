package com.inclinic.app.features.patient.medical_history.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.ShareRequest
import com.inclinic.app.features.patient.infrastructure.remote.ShareDataSource
import kotlinx.coroutines.withContext

class RevokeAccessUseCase(
    private val dataSource: ShareDataSource,
    private val dispatchers: AppDispatchers,
) {
    /** Revoke an approved share grant. Backend: DELETE /api/medical-history-share/{requestId} */
    suspend operator fun invoke(requestId: String): Result<ShareRequest> =
        withContext(dispatchers.io) {
            dataSource.revokeAccess(requestId)
        }
}
