package com.inclinic.app.core.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.tasks.await

@Composable
actual fun rememberLocationProvider(): LocationProvider {
    val context = LocalContext.current

    // Puente permiso-callback → suspensión: cada solicitud crea un Deferred que el launcher resuelve.
    var pending: CompletableDeferred<Boolean>? = null
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        pending?.complete(granted)
    }

    return remember(context) {
        object : LocationProvider {
            override suspend fun getCurrentLocation(): LocationResult {
                if (!hasLocationPermission(context)) {
                    val deferred = CompletableDeferred<Boolean>()
                    pending = deferred
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    val granted = deferred.await()
                    pending = null
                    if (!granted) return LocationResult.PermissionDenied
                }

                if (!isLocationEnabled(context)) {
                    return LocationResult.Unavailable("La ubicación del dispositivo está desactivada")
                }

                return try {
                    val client = LocationServices.getFusedLocationProviderClient(context)
                    val request = CurrentLocationRequest.Builder()
                        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                        .build()
                    @Suppress("MissingPermission")
                    val location = client.getCurrentLocation(request, null).await()
                    if (location == null) {
                        LocationResult.Unavailable("No se pudo obtener la ubicación")
                    } else {
                        LocationResult.Success(
                            GpsFix(
                                lat = location.latitude,
                                lng = location.longitude,
                                accuracyMeters = if (location.hasAccuracy()) location.accuracy.toDouble() else null,
                            ),
                        )
                    }
                } catch (e: Exception) {
                    LocationResult.Unavailable(e.message ?: "Error obteniendo la ubicación")
                }
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

private fun isLocationEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
