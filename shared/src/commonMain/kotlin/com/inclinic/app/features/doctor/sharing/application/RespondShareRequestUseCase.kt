package com.inclinic.app.features.doctor.sharing.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.sharing.core.port.DoctorSharingRepository
import kotlinx.coroutines.withContext

/**
 * Doctor cancels their own PENDING share request.
 * The patient-side respond (approve/reject) is handled via patient flows.
 */
class RespondShareRequestUseCase(
    private val repository: DoctorSharingRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(id: String): Result<Unit> =
        withContext(dispatchers.io) { repository.cancelRequest(id) }
}
