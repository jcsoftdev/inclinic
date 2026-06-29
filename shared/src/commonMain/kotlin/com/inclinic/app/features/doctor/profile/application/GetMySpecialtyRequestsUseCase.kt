package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.core.model.MySpecialtyRequest
import com.inclinic.app.features.doctor.profile.core.port.DoctorProfileRepository
import kotlinx.coroutines.withContext

class GetMySpecialtyRequestsUseCase(
    private val repository: DoctorProfileRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<MySpecialtyRequest>> =
        withContext(dispatchers.io) { repository.getMySpecialtyRequests() }
}
