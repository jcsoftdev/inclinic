package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CompletableDeferred
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.darwin.NSObject

/**
 * LocationProvider para iOS basado en [CLLocationManager].
 *
 * Requiere `NSLocationWhenInUseUsageDescription` en Info.plist.
 * NOTA: compila, pero el interop CoreLocation requiere prueba en device/simulador — no se
 * valida en CI.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberLocationProvider(): LocationProvider {
    val delegate = remember { IosLocationDelegate() }
    return remember(delegate) {
        object : LocationProvider {
            override suspend fun getCurrentLocation(): LocationResult = delegate.requestLocation()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosLocationDelegate : NSObject(), CLLocationManagerDelegateProtocol {

    private val manager = CLLocationManager()
    private var permissionDeferred: CompletableDeferred<Boolean>? = null
    private var locationDeferred: CompletableDeferred<LocationResult>? = null

    init {
        manager.delegate = this
    }

    suspend fun requestLocation(): LocationResult {
        val status = manager.authorizationStatus
        val authorized = when (status) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways,
            -> true
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted,
            -> return LocationResult.PermissionDenied
            kCLAuthorizationStatusNotDetermined -> {
                val deferred = CompletableDeferred<Boolean>()
                permissionDeferred = deferred
                manager.requestWhenInUseAuthorization()
                val granted = deferred.await()
                permissionDeferred = null
                granted
            }
            else -> false
        }
        if (!authorized) return LocationResult.PermissionDenied

        val deferred = CompletableDeferred<LocationResult>()
        locationDeferred = deferred
        manager.requestLocation()
        val result = deferred.await()
        locationDeferred = null
        return result
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        val status = manager.authorizationStatus
        val granted = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
        if (status != kCLAuthorizationStatusNotDetermined) {
            permissionDeferred?.complete(granted)
        }
    }

    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        if (location == null) {
            locationDeferred?.complete(LocationResult.Unavailable("Sin ubicación"))
            return
        }
        val fix = location.coordinate.useContents {
            GpsFix(
                lat = latitude,
                lng = longitude,
                accuracyMeters = location.horizontalAccuracy.takeIf { it >= 0 },
            )
        }
        locationDeferred?.complete(LocationResult.Success(fix))
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
        locationDeferred?.complete(
            LocationResult.Unavailable(didFailWithError.localizedDescription),
        )
    }
}

@Suppress("unused")
private fun CLAuthorizationStatus.ignore() = Unit
