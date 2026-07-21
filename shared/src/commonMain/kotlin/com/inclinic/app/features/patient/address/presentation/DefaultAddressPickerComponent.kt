package com.inclinic.app.features.patient.address.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.error.toUserMessage
import com.inclinic.app.features.patient.address.application.ReverseGeocodeUseCase
import com.inclinic.app.features.patient.address.application.SearchAddressUseCase
import com.inclinic.app.features.patient.address.infrastructure.GeocodeSuggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DefaultAddressPickerComponent(
    componentContext: ComponentContext,
    private val searchAddress: SearchAddressUseCase,
    private val reverseGeocode: ReverseGeocodeUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AddressPickerComponent.Output) -> Unit,
) : AddressPickerComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AddressPickerState())
    override val state: Value<AddressPickerState> = _state

    private var searchJob: Job? = null

    override fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, error = null) }
        searchJob?.cancel()
        if (query.trim().length < 3) {
            _state.update { it.copy(suggestions = emptyList(), isSearching = false) }
            return
        }
        // Debounce ~400 ms para respetar el rate-limit de Nominatim (server-side cachea).
        searchJob = scope.launch {
            delay(DEBOUNCE_MS)
            _state.update { it.copy(isSearching = true) }
            searchAddress(query)
                .onSuccess { results -> _state.update { it.copy(isSearching = false, suggestions = results) } }
                .onFailure { err -> _state.update { it.copy(isSearching = false, error = err.toUserMessage("No se pudo buscar la dirección")) } }
        }
    }

    override fun onSuggestionSelected(suggestion: GeocodeSuggestion) {
        _state.update {
            it.copy(
                query = suggestion.displayName,
                suggestions = emptyList(),
                selectedLat = suggestion.lat,
                selectedLng = suggestion.lng,
                selectedAddress = suggestion.displayName,
                error = null,
            )
        }
    }

    override fun onMarkerMoved(lat: Double, lng: Double) {
        _state.update { it.copy(selectedLat = lat, selectedLng = lng, isResolvingPin = true, error = null) }
        scope.launch {
            reverseGeocode(lat, lng)
                .onSuccess { suggestion ->
                    _state.update {
                        it.copy(
                            isResolvingPin = false,
                            selectedAddress = suggestion?.displayName ?: it.selectedAddress,
                            query = suggestion?.displayName ?: it.query,
                        )
                    }
                }
                .onFailure { err -> _state.update { it.copy(isResolvingPin = false, error = err.toUserMessage("No se pudo obtener la dirección del punto")) } }
        }
    }

    override fun onConfirm() {
        val s = _state.value
        val lat = s.selectedLat ?: return
        val lng = s.selectedLng ?: return
        val address = s.selectedAddress?.takeIf { it.isNotBlank() } ?: return
        onOutput(AddressPickerComponent.Output.Confirmed(address, lat, lng))
    }

    override fun onBack() { onOutput(AddressPickerComponent.Output.Back) }

    private companion object {
        const val DEBOUNCE_MS = 400L
    }
}
