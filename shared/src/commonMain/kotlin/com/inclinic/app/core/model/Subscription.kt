package com.inclinic.app.core.model

enum class SubscriptionTier { FREE, PREMIUM }

/**
 * Estado de la membresía del paciente.
 *
 * [expiresAt] se mantiene como String ISO-8601 (igual que llega del backend) para
 * simplicidad; la UI solo lo muestra. Null cuando nunca hubo Premium.
 */
data class Subscription(
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val expiresAt: String? = null,
)
