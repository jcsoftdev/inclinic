package com.inclinic.app.features.doctor.therapy_offers.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.therapy_offers.core.model.NewOfferDraft
import com.inclinic.app.features.doctor.therapy_offers.core.model.TherapyOffer
import com.inclinic.app.features.doctor.therapy_offers.core.port.DoctorTherapyOffersRepository

class CreateTherapyOfferUseCase(
    private val repository: DoctorTherapyOffersRepository,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(draft: NewOfferDraft): Result<TherapyOffer> = repository.createOffer(draft)
}
