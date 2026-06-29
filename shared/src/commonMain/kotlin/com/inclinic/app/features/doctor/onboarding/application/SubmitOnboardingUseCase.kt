package com.inclinic.app.features.doctor.onboarding.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.DoctorOnboardingDraft
import com.inclinic.app.features.doctor.onboarding.core.port.DoctorOnboardingRepository
import kotlinx.coroutines.withContext

class SubmitOnboardingUseCase(
    private val repository: DoctorOnboardingRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(draft: DoctorOnboardingDraft): Result<Unit> =
        withContext(dispatchers.io) { repository.submit(draft) }
}
