package com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote

import com.inclinic.app.core.network.ApiEnvelope
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.dto.CreateOfferRequestDto
import com.inclinic.app.features.doctor.therapy_offers.infrastructure.remote.dto.TherapyOfferDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorDoctorTherapyOffersDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : DoctorTherapyOffersDataSource {

    /** GET /api/doctors/me/therapy-offers -- returns the authenticated doctor's own offers (incl. inactive). */
    override suspend fun listMyOffers(): Result<List<TherapyOfferDto>> = runCatching {
        client.get {
            url("$baseUrl/api/doctors/me/therapy-offers")
        }.body<ApiEnvelope<List<TherapyOfferDto>>>().data ?: emptyList()
    }

    /** POST /api/therapy-offers -- creates a new public offer template. */
    override suspend fun createOffer(body: CreateOfferRequestDto): Result<TherapyOfferDto> = runCatching {
        client.post {
            url("$baseUrl/api/therapy-offers")
            contentType(ContentType.Application.Json)
            setBody(body)
        }.body<ApiEnvelope<TherapyOfferDto>>().data ?: error("No data in create offer response")
    }
}
