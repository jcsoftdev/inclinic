package com.inclinic.app.features.patient.subscription.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.RawCard
import com.inclinic.app.core.model.Subscription
import com.inclinic.app.core.port.CardTokenizer
import com.inclinic.app.features.patient.infrastructure.remote.SubscriptionDataSource
import kotlinx.coroutines.withContext

class PurchasePremiumUseCase(
    private val cardTokenizer: CardTokenizer,
    private val dataSource: SubscriptionDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(rawCard: RawCard): Result<Subscription> =
        withContext(dispatchers.io) {
            val tokenResult = cardTokenizer.tokenize(rawCard)
            if (tokenResult.isFailure) return@withContext Result.failure(tokenResult.exceptionOrNull()!!)
            val token = tokenResult.getOrThrow()
            val paymentMethodId = token.brand.lowercase()
            dataSource.purchasePremium(token.token, paymentMethodId)
        }
}
