package com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote

import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.dto.CreateOfferRequestDto
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.dto.TherapyOfferDto

interface DoctorTherapyOffersDataSource {
    suspend fun listMyOffers(): Result<List<TherapyOfferDto>>
    suspend fun createOffer(body: CreateOfferRequestDto): Result<TherapyOfferDto>
}
