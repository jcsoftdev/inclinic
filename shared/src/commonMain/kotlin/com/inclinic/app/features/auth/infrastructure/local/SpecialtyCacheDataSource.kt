package com.inclinic.app.features.auth.infrastructure.local

import com.inclinic.app.core.model.Specialty
import com.inclinic.app.features.auth.infrastructure.remote.AuthRemoteDataSource
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.hours

/**
 * In-memory specialty cache with a 24-hour TTL.
 *
 * Strategy:
 * - On first call (or when TTL has expired) the list is fetched from [remote] and
 *   stored together with the fetch timestamp.
 * - Subsequent calls within the TTL return the cached list immediately without
 *   hitting the network.
 * - The cache is intentionally in-memory only; it resets on process restart,
 *   which is acceptable given the 24-hour TTL and the low cost of a single
 *   GET /api/specialties call.
 *
 * Thread-safety: Access must happen on the same coroutine dispatcher (callers
 * are expected to already be on [AppDispatchers.io]).
 *
 * REQ-4-003
 */
class SpecialtyCacheDataSource(
    private val remote: AuthRemoteDataSource,
    private val clock: Clock = Clock.System,
) {
    private var cachedSpecialties: List<Specialty>? = null
    private var cachedAt: Instant? = null

    suspend fun getSpecialties(): Result<List<Specialty>> {
        val now = clock.now()
        val cached = cachedSpecialties
        val at = cachedAt
        if (cached != null && at != null && (now - at) < 24.hours) {
            return Result.success(cached)
        }
        return remote.getSpecialties().onSuccess { list ->
            cachedSpecialties = list
            cachedAt = now
        }
    }

    /** Force-invalidate the cache (e.g., after an admin update). */
    fun invalidate() {
        cachedSpecialties = null
        cachedAt = null
    }
}
