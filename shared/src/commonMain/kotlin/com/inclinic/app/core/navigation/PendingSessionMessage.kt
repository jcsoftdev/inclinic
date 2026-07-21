package com.inclinic.app.core.navigation

/**
 * In-memory holder for a "session expired" notice that must survive the
 * Auth → Login navigation triggered by [DefaultRootComponent].
 *
 * Lifecycle:
 *  1. [DefaultRootComponent] sets [expired] to `true` only when
 *     [com.inclinic.app.core.events.SessionExpiryReason.EXPIRED] fires (a real
 *     401/token-expiry) — never for an explicit, user-initiated logout.
 *  2. The next [com.inclinic.app.features.auth.presentation.component.DefaultLoginComponent]
 *     constructed consumes and clears the flag, surfacing
 *     [com.inclinic.app.features.auth.core.error.SessionExpiredMessage] in its state.
 *
 * Mirrors [PendingDeepLink]'s pattern for carrying state across a stack replace.
 * Thread-safety: access must happen on the main thread (Decompose contract).
 */
object PendingSessionMessage {
    var expired: Boolean = false
}
