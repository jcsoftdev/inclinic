package com.inclinic.app.features.patient.infrastructure.remote

import com.inclinic.app.core.model.Subscription
import com.inclinic.app.core.model.SubscriptionTier
import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

class KtorSubscriptionDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : SubscriptionDataSource {

    override suspend fun getSubscription(): Result<Subscription> = runCatching {
        client.get {
            url("$baseUrl/api/subscriptions/me")
        }.body<ApiEnvelope<SubscriptionDto>>().data?.toDomain() ?: Subscription()
    }

    override suspend fun purchasePremium(cardToken: String, paymentMethodId: String): Result<Subscription> = runCatching {
        client.post {
            url("$baseUrl/api/subscriptions/purchase")
            contentType(ContentType.Application.Json)
            setBody(PurchasePremiumBody(cardToken = cardToken, paymentMethodId = paymentMethodId))
        }.body<ApiEnvelope<SubscriptionDto>>().data?.toDomain() ?: error("Purchase failed")
    }
}

// ── DTOs ──────────────────────────────────────────────────────────────────────

@Serializable
private data class PurchasePremiumBody(
    val cardToken: String,
    val paymentMethodId: String,
)

@Serializable
private data class SubscriptionDto(
    val tier: String = "FREE",
    val expiresAt: String? = null,
) {
    fun toDomain(): Subscription = Subscription(
        tier = runCatching { SubscriptionTier.valueOf(tier) }.getOrDefault(SubscriptionTier.FREE),
        expiresAt = expiresAt,
    )
}
