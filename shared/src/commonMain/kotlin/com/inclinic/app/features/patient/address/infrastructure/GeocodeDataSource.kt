package com.inclinic.app.features.patient.address.infrastructure

import kotlinx.serialization.Serializable

/** Un candidato de dirección devuelto por el geocoding (texto + coordenadas). */
@Serializable
data class GeocodeSuggestion(
    val displayName: String,
    val lat: Double,
    val lng: Double,
)

/**
 * Fuente de geocoding. Pega al proxy del backend (Nominatim + caché), nunca a OSM directo.
 */
interface GeocodeDataSource {
    /** Autocompletado: texto → candidatos. */
    suspend fun search(query: String, limit: Int = 5): Result<List<GeocodeSuggestion>>

    /** Geocoding inverso: coordenadas → dirección (o null si no hay match). */
    suspend fun reverse(lat: Double, lng: Double): Result<GeocodeSuggestion?>
}
