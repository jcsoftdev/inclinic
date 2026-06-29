package com.inclinic.app.features.admin.reviews.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class UnhideReviewUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(appointmentId: String): Result<Unit> =
        withContext(dispatchers.io) {
            dataSource.unhideReview(appointmentId)
        }
}
