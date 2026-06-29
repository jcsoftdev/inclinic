package com.inclinic.app.features.admin.reviews.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import com.inclinic.app.features.admin.infrastructure.remote.AdminReviewItem
import kotlinx.coroutines.withContext

class GetReviewsUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(
        withComment: Boolean? = null,
        hidden: Boolean? = null,
    ): Result<List<AdminReviewItem>> =
        withContext(dispatchers.io) {
            dataSource.getReviews(withComment = withComment, hidden = hidden)
        }
}
