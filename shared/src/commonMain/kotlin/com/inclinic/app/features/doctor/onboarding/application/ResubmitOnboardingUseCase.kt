package com.inclinic.app.features.doctor.onboarding.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.port.DoctorOnboardingRepository
import kotlinx.coroutines.withContext

class ResubmitOnboardingUseCase(
    private val repository: DoctorOnboardingRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(corrections: Map<String, String>): Result<Unit> =
        withContext(dispatchers.io) { repository.resubmit(corrections) }
}
