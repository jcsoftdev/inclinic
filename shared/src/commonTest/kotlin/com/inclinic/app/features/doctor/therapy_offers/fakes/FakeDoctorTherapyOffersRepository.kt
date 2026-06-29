package com.inclinic.app.features.doctor.therapy_offers.fakes

import com.inclinic.app.features.doctor.therapy_offers.core.model.NewOfferDraft
import com.inclinic.app.features.doctor.therapy_offers.core.model.TherapyOffer
import com.inclinic.app.features.doctor.therapy_offers.core.port.DoctorTherapyOffersRepository

class FakeDoctorTherapyOffersRepository : DoctorTherapyOffersRepository {
    var offersResult: Result<List<TherapyOffer>> = Result.success(emptyList())
    var createResult: Result<TherapyOffer> = Result.success(offerFixture())
    var lastCreatedDraft: NewOfferDraft? = null

    override suspend fun getMyOffers(): Result<List<TherapyOffer>> = offersResult

    override suspend fun createOffer(draft: NewOfferDraft): Result<TherapyOffer> {
        lastCreatedDraft = draft
        return createResult
    }
}

fun offerFixture(
    id: String = "offer-1",
    title: String = "Cardio Premium",
    specialtyId: String = "spe-1",
    specialtyName: String = "Cardiologia",
    totalSessions: Int = 8,
    pricePerSession: Double = 80.0,
    isActive: Boolean = true,
) = TherapyOffer(
    id = id,
    title = title,
    specialtyId = specialtyId,
    specialtyName = specialtyName,
    totalSessions = totalSessions,
    pricePerSession = pricePerSession,
    minPricePerSession = null,
    sessionDurationMin = 45,
    description = null,
    isActive = isActive,
)
