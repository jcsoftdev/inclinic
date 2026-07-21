package com.inclinic.app.features.patient.address.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.address.infrastructure.GeocodeDataSource
import com.inclinic.app.features.patient.address.infrastructure.GeocodeSuggestion
import kotlinx.coroutines.withContext

class ReverseGeocodeUseCase(
    private val dataSource: GeocodeDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(lat: Double, lng: Double): Result<GeocodeSuggestion?> =
        withContext(dispatchers.io) { dataSource.reverse(lat, lng) }
}
