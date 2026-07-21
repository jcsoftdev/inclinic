package com.inclinic.app.features.patient.address.presentation

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.patient.address.infrastructure.GeocodeSuggestion
import kotlinx.serialization.Serializable

interface AddressPickerComponent {
    val state: Value<AddressPickerState>

    /** El usuario escribe en el buscador (dispara autocompletado con debounce). */
    fun onQueryChange(query: String)

    /** El usuario elige una sugerencia del autocompletado (texto → pin). */
    fun onSuggestionSelected(suggestion: GeocodeSuggestion)

    /** El usuario mueve el pin o toca el mapa (pin → texto vía geocoding inverso). */
    fun onMarkerMoved(lat: Double, lng: Double)

    /** Confirma la dirección seleccionada y continúa a Booking. */
    fun onConfirm()

    fun onBack()

    sealed interface Output {
        data class Confirmed(val address: String, val lat: Double, val lng: Double) : Output
        data object Back : Output
    }
}

@Serializable
data class AddressPickerState(
    val query: String = "",
    val suggestions: List<GeocodeSuggestion> = emptyList(),
    val isSearching: Boolean = false,
    /** Coordenada actualmente seleccionada (centro del mapa + pin). Null hasta la primera elección. */
    val selectedLat: Double? = null,
    val selectedLng: Double? = null,
    /** Texto de dirección confirmado para el pin actual. */
    val selectedAddress: String? = null,
    val isResolvingPin: Boolean = false,
    val error: String? = null,
) {
    val canConfirm: Boolean
        get() = selectedLat != null && selectedLng != null && !selectedAddress.isNullOrBlank()
}
