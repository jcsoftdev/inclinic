package com.inclinic.app.features.doctor.therapy_offers.infrastructure

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.therapy_offers.core.model.NewOfferDraft
import com.inclinic.app.features.doctor.therapy_offers.core.model.TherapyOffer
import com.inclinic.app.features.doctor.therapy_offers.core.port.DoctorTherapyOffersRepository
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.DoctorTherapyOffersDataSource
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.dto.CreateOfferRequestDto
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.dto.TherapyOfferDto
import kotlinx.coroutines.withContext

class DefaultDoctorTherapyOffersRepository(
    private val remote: DoctorTherapyOffersDataSource,
    private val dispatchers: AppDispatchers,
) : DoctorTherapyOffersRepository {

    override suspend fun getMyOffers(): Result<List<TherapyOffer>> =
        withContext(dispatchers.io) {
            remote.listMyOffers().map { list -> list.map(::toDomain) }
        }

    override suspend fun createOffer(draft: NewOfferDraft): Result<TherapyOffer> =
        withContext(dispatchers.io) {
            val body = CreateOfferRequestDto(
                specialtyId = draft.specialtyId,
                title = draft.title,
                description = draft.description,
                totalSessions = draft.totalSessions,
                pricePerSession = draft.pricePerSession,
                minPricePerSession = draft.minPricePerSession,
                sessionDurationMin = draft.sessionDurationMin,
                isActive = draft.isActive,
            )
            remote.createOffer(body).map(::toDomain)
        }

    private fun toDomain(dto: TherapyOfferDto) = TherapyOffer(
        id = dto.id,
        title = dto.title,
        specialtyId = dto.specialtyId,
        specialtyName = dto.specialty?.name ?: "",
        totalSessions = dto.totalSessions,
        pricePerSession = dto.pricePerSession,
        minPricePerSession = dto.minPricePerSession,
        sessionDurationMin = dto.sessionDurationMin,
        description = dto.description,
        isActive = dto.isActive,
    )
}
