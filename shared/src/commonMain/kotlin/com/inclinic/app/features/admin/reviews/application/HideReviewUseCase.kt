package com.inclinic.app.features.admin.reviews.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class HideReviewUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(appointmentId: String, reason: String): Result<Unit> =
        withContext(dispatchers.io) {
            dataSource.hideReview(appointmentId, reason)
        }
}
