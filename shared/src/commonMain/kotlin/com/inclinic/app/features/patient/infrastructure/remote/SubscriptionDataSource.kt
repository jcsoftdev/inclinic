package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Subscription

interface SubscriptionDataSource {
    suspend fun getSubscription(): Result<Subscription>
    suspend fun purchasePremium(cardToken: String, paymentMethodId: String): Result<Subscription>
}
