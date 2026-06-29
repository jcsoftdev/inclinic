package com.inclinic.app.core.navigation

/**
 * In-memory holder for a deep link that arrived before the target flow was active.
 *
 * Lifecycle:
 *  1. [DefaultRootComponent.handleDeepLink] stores the link here when the destination
 *     flow is not yet mounted.
 *  2. Once the target flow is created, [DefaultRootComponent] drains the pending link
 *     and navigates within the flow.
 *
 * Thread-safety: Access must happen on the main thread (Decompose contract).
 */
object PendingDeepLink {
    var link: DeepLink? = null
}
