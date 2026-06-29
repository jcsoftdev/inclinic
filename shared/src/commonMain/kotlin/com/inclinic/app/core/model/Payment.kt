package com.inclinic.app.core.model

data class RawCard(
    val pan: String,
    val cvv: String,
    val expMonth: Int,
    val expYear: Int,
    val holderName: String,
    val docType: String,
    val docNumber: String,
)

data class CardToken(
    val token: String,
    val last4: String,
    val brand: String,
)
