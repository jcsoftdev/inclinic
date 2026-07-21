package com.inclinic.app.features.patient.address.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.core.platform.LeafletMapView
import com.inclinic.app.core.platform.MapCoordinate
import com.inclinic.app.features.patient.address.infrastructure.GeocodeSuggestion

@Composable
fun AddressPickerScreen(component: AddressPickerComponent) {
    val state by component.state.subscribeAsState()

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = component::onBack) { Text("Atrás") }

        Text("¿A qué dirección va el médico?", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = state.query,
            onValueChange = component::onQueryChange,
            label = { Text("Buscar dirección") },
            trailingIcon = { if (state.isSearching) CircularProgressIndicator(Modifier.height(20.dp)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (state.suggestions.isNotEmpty()) {
            LazyColumn(Modifier.fillMaxWidth().height(160.dp)) {
                items(state.suggestions) { s: GeocodeSuggestion ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { component.onSuggestionSelected(s) }
                            .padding(vertical = 10.dp),
                    ) {
                        Text(
                            s.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    HorizontalDivider()
                }
            }
        }

        // El mapa aparece una vez elegida una primera ubicación (texto → pin).
        val lat = state.selectedLat
        val lng = state.selectedLng
        if (lat != null && lng != null) {
            Box(Modifier.fillMaxWidth().height(280.dp)) {
                LeafletMapView(
                    center = MapCoordinate(lat, lng),
                    onMarkerMoved = { coord -> component.onMarkerMoved(coord.lat, coord.lng) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            state.selectedAddress?.let {
                Text(
                    if (state.isResolvingPin) "Obteniendo dirección del punto…" else it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Text(
                "Busca tu dirección y luego ajusta el pin en el mapa para confirmar el punto exacto.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = component::onConfirm,
            enabled = state.canConfirm && !state.isResolvingPin,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text("Confirmar dirección")
        }
    }
}
