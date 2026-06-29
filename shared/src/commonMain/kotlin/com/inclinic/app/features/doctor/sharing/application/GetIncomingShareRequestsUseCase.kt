package com.inclinic.app.features.doctor.sharing.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequest
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequestStatus
import com.inclinic.app.features.doctor.sharing.core.port.DoctorSharingRepository
import kotlinx.coroutines.withContext

/**
 * Returns the doctor's share requests that are APPROVED (active access granted).
 * "Incoming" from the doctor's perspective = patients have granted access.
 */
class GetIncomingShareRequestsUseCase(
    private val repository: DoctorSharingRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<ShareRequest>> =
        withContext(dispatchers.io) {
            repository.listRequests().map { list ->
                list.filter { it.status == ShareRequestStatus.APPROVED }
            }
        }
}
