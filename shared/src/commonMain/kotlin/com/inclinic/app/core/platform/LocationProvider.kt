package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable

/** Una lectura de GPS: coordenadas + precisión reportada por el dispositivo, en metros. */
data class GpsFix(
    val lat: Double,
    val lng: Double,
    val accuracyMeters: Double?,
)

/** Resultado de pedir la ubicación actual. */
sealed interface LocationResult {
    data class Success(val fix: GpsFix) : LocationResult

    /** El usuario negó (o no concedió) el permiso de ubicación. */
    data object PermissionDenied : LocationResult

    /** El permiso está, pero no se pudo obtener un fix (GPS apagado, timeout, error del proveedor). */
    data class Unavailable(val reason: String) : LocationResult
}

/**
 * Pide la ubicación actual del dispositivo. Solicita el permiso de ubicación si hace falta.
 * Suspende hasta obtener un resultado (fix, permiso negado o no disponible).
 */
interface LocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}

/**
 * Recuerda un [LocationProvider] por plataforma.
 *
 * Android: FusedLocationProvider + solicitud de ACCESS_FINE_LOCATION vía ActivityResult.
 * iOS: CLLocationManager + NSLocationWhenInUseUsageDescription (requiere prueba en device;
 * el interop no se valida en CI).
 */
@Composable
expect fun rememberLocationProvider(): LocationProvider
