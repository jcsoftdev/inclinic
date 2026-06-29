package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.core.model.DoctorProfile
import com.inclinic.app.features.doctor.profile.core.port.DoctorProfileRepository
import kotlinx.coroutines.withContext

class GetDoctorProfileUseCase(
    private val repository: DoctorProfileRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<DoctorProfile> =
        withContext(dispatchers.io) { repository.getProfile() }
}
