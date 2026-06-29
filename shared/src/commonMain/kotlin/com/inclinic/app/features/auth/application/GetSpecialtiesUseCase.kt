package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Specialty
import com.inclinic.app.features.auth.infrastructure.local.SpecialtyCacheDataSource
import kotlinx.coroutines.withContext

/**
 * Returns the specialty list, reading from the 24-hour in-memory cache when possible.
 *
 * REQ-4-003: specialty cache with 24h TTL.
 */
class GetSpecialtiesUseCase(
    private val cache: SpecialtyCacheDataSource,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke(): Result<List<Specialty>> = withContext(dispatchers.io) {
        cache.getSpecialties()
    }
}
