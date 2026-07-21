package com.inclinic.app.core.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Why the session was ended — lets subscribers (namely [com.inclinic.app.core.navigation.DefaultRootComponent])
 * distinguish a real 401/token-expiry from an explicit user-initiated logout, so only the
 * former surfaces "tu sesión expiró" at Login.
 */
enum class SessionExpiryReason {
    /** Token refresh failed / a 401 was received after refresh (real session expiry). */
    EXPIRED,
    /** User explicitly tapped "Cerrar sesión" — no message should be shown. */
    USER_INITIATED,
}

class SessionEvents {
    private val _expired = MutableSharedFlow<SessionExpiryReason>(extraBufferCapacity = 1)
    val expired: SharedFlow<SessionExpiryReason> = _expired.asSharedFlow()

    fun emitExpired(reason: SessionExpiryReason = SessionExpiryReason.EXPIRED) {
        _expired.tryEmit(reason)
    }
}
