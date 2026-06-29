package com.inclinic.app.features.doctor.therapy_offers.core.model

data class TherapyOffer(
    val id: String,
    val title: String,
    val specialtyId: String,
    val specialtyName: String,
    val totalSessions: Int,
    val pricePerSession: Double,
    val minPricePerSession: Double?,
    val sessionDurationMin: Int,
    val description: String?,
    val isActive: Boolean,
)

data class NewOfferDraft(
    val title: String,
    val specialtyId: String,
    val totalSessions: Int,
    val pricePerSession: Double,
    val minPricePerSession: Double?,
    val sessionDurationMin: Int?,
    val description: String?,
    val isActive: Boolean,
)
