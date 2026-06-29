package com.inclinic.app.features.admin.subscriptions.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminDataSource
import kotlinx.coroutines.withContext

class SetUserSubscriptionUseCase(
    private val dataSource: AdminDataSource,
    private val dispatchers: AppDispatchers,
) {
    /**
     * Changes a user's subscription tier.
     *
     * @param userId  The User.id (auth id, not the Doctor.id).
     * @param tier    "FREE" or "PREMIUM".
     * @param expiresAt Optional ISO 8601 datetime string for when the subscription expires.
     *                  Pass null for no expiry (permanent while tier holds).
     */
    suspend operator fun invoke(
        userId: String,
        tier: String,
        expiresAt: String? = null,
    ): Result<Unit> =
        withContext(dispatchers.io) { dataSource.setUserSubscription(userId, tier, expiresAt) }
}
