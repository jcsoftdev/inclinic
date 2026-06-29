package com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.dto

import kotlinx.serialization.Serializable

/**
 * Mirrors TherapyPackageOffer from the backend.
 * GET /api/doctors/me/therapy-offers  ->  {success, data: [...]}
 * POST /api/therapy-offers            ->  {success, data: {...}}
 */
@Serializable
data class TherapyOfferDto(
    val id: String,
    val title: String,
    val specialtyId: String,
    val totalSessions: Int,
    val pricePerSession: Double,
    val minPricePerSession: Double? = null,
    val sessionDurationMin: Int = 45,
    val description: String? = null,
    val isActive: Boolean = true,
    val specialty: OfferSpecialtyDto? = null,
)

@Serializable
data class OfferSpecialtyDto(
    val id: String? = null,
    val name: String? = null,
)

/**
 * POST /api/therapy-offers body.
 * Matches CreateOfferInput in packageOffer.service.ts.
 */
@Serializable
data class CreateOfferRequestDto(
    val specialtyId: String,
    val title: String,
    val description: String? = null,
    val totalSessions: Int,
    val pricePerSession: Double,
    val minPricePerSession: Double? = null,
    val sessionDurationMin: Int? = null,
    val isActive: Boolean = true,
)
