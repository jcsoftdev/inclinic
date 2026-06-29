package com.inclinic.app.features.doctor.profile.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.profile.core.model.IncomeSummary
import com.inclinic.app.features.doctor.profile.core.port.DoctorProfileRepository
import kotlinx.coroutines.withContext

/**
 * Fetches the doctor's income summary from GET /api/doctors/me/metrics.
 *
 * Note: The backend only provides current-month aggregates (no date range selection,
 * no time-series bars). The returned [IncomeSummary] will have empty [IncomeSummary.bars].
 */
class GetDoctorIncomeUseCase(
    private val repository: DoctorProfileRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<IncomeSummary> =
        withContext(dispatchers.io) { repository.getIncome() }
}
