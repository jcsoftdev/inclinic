package com.inclinic.app.features.patient.subscription.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Subscription
import com.inclinic.app.features.patient.infrastructure.remote.SubscriptionDataSource
import kotlinx.coroutines.withContext

class GetSubscriptionUseCase(
    private val dataSource: SubscriptionDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<Subscription> =
        withContext(dispatchers.io) {
            dataSource.getSubscription()
        }
}
