package com.inclinic.app.features.doctor.therapy_offers.core.port

import com.inclinic.app.features.doctor.therapy_offers.core.model.NewOfferDraft
import com.inclinic.app.features.doctor.therapy_offers.core.model.TherapyOffer

interface DoctorTherapyOffersRepository {
    suspend fun getMyOffers(): Result<List<TherapyOffer>>
    suspend fun createOffer(draft: NewOfferDraft): Result<TherapyOffer>
}
