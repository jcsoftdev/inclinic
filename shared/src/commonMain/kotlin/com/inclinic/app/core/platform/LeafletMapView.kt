package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Una coordenada geográfica simple para el mapa. */
data class MapCoordinate(val lat: Double, val lng: Double)

/**
 * Mapa Leaflet (OpenStreetMap) con un pin arrastrable, embebido en un WebView por plataforma.
 *
 * - [center] fija la vista inicial y, si cambia desde afuera (p. ej. tras un geocoding de
 *   texto→coordenadas), reposiciona el pin vía el puente nativo→JS.
 * - [onMarkerMoved] se dispara cuando el usuario arrastra el pin o toca el mapa (pin→texto).
 *
 * Requiere internet (tiles + librería Leaflet vía CDN). El interop de iOS (WKWebView) no se
 * valida en CI; probar en device/simulador.
 */
@Composable
expect fun LeafletMapView(
    center: MapCoordinate,
    onMarkerMoved: (MapCoordinate) -> Unit,
    modifier: Modifier = Modifier,
)
