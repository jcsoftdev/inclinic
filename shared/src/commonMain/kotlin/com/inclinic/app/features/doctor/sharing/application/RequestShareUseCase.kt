package com.inclinic.app.features.doctor.sharing.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequest
import com.inclinic.app.features.doctor.sharing.core.port.DoctorSharingRepository
import kotlinx.coroutines.withContext

class RequestShareUseCase(
    private val repository: DoctorSharingRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        patientId: String,
        reason: String,
        scope: String = "FULL_HISTORY",
    ): Result<ShareRequest> =
        withContext(dispatchers.io) { repository.requestShare(patientId, reason, scope) }
}
