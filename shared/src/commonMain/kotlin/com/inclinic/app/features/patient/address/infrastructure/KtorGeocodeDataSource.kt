package com.inclinic.app.features.patient.address.infrastructure

import com.inclinic.app.core.network.ApiEnvelope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.serialization.Serializable

@Serializable
private data class GeocodeDto(
    val displayName: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
)

private fun GeocodeDto.toDomain() = GeocodeSuggestion(displayName = displayName, lat = lat, lng = lng)

class KtorGeocodeDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : GeocodeDataSource {

    override suspend fun search(query: String, limit: Int): Result<List<GeocodeSuggestion>> = runCatching {
        client.get {
            url("$baseUrl/api/geocoding/search")
            parameter("q", query)
            parameter("limit", limit)
        }.body<ApiEnvelope<List<GeocodeDto>>>().data.orEmpty().map { it.toDomain() }
    }

    override suspend fun reverse(lat: Double, lng: Double): Result<GeocodeSuggestion?> = runCatching {
        client.get {
            url("$baseUrl/api/geocoding/reverse")
            parameter("lat", lat)
            parameter("lng", lng)
        }.body<ApiEnvelope<GeocodeDto?>>().data?.toDomain()
    }
}
