package com.inclinic.app.features.auth.application

import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.events.SessionEvents
import com.inclinic.app.core.events.SessionExpiryReason
import com.inclinic.app.features.auth.core.port.TokenStorage
import kotlinx.coroutines.withContext

/**
 * Clears locally stored tokens and emits a SessionExpired event so the root
 * component navigates back to Auth without the caller needing to handle navigation.
 * Does NOT make any network call (no server-side token revocation in v1).
 * Idempotent: safe to call even when no tokens are stored.
 */
class LogoutUseCase(
    private val tokenStorage: TokenStorage,
    private val sessionEvents: SessionEvents,
    private val dispatchers: AppDispatchers,
) {
    suspend operator fun invoke() = withContext(dispatchers.io) {
        tokenStorage.clear()
        sessionEvents.emitExpired(SessionExpiryReason.USER_INITIATED)
    }
}
