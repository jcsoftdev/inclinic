package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequest
import com.inclinic.app.features.doctor.profile.core.port.DoctorProfileRepository
import kotlinx.coroutines.withContext

class RequestSpecialtyUseCase(
    private val repository: DoctorProfileRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(request: SpecialtyRequest): Result<Unit> =
        withContext(dispatchers.io) { repository.requestSpecialty(request) }
}
