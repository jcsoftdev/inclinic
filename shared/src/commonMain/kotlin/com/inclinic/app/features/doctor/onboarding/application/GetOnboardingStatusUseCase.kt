package com.inclinic.app.features.doctor.onboarding.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.OnboardingStatus
import com.inclinic.app.features.doctor.onboarding.core.port.DoctorOnboardingRepository
import kotlinx.coroutines.withContext

class GetOnboardingStatusUseCase(
    private val repository: DoctorOnboardingRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<OnboardingStatus> =
        withContext(dispatchers.io) { repository.getStatus() }
}
