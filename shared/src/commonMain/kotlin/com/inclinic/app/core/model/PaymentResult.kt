package com.inclinic.app.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PaymentResult(
    val appointmentId: String,
    val status: String = "approved",
    val transactionId: String? = null,
)
