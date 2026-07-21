package com.inclinic.app.features.patient.address.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.address.infrastructure.GeocodeDataSource
import com.inclinic.app.features.patient.address.infrastructure.GeocodeSuggestion
import kotlinx.coroutines.withContext

class SearchAddressUseCase(
    private val dataSource: GeocodeDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(query: String): Result<List<GeocodeSuggestion>> =
        withContext(dispatchers.io) {
            if (query.trim().length < 3) Result.success(emptyList())
            else dataSource.search(query)
        }
}
