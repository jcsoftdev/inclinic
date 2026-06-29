package com.inclinic.app.core.concurrency

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Production [AppDispatchers] implementation using real Kotlinx coroutines dispatchers.
 *
 * - [main]    — Dispatchers.Main (UI thread on Android; main thread on iOS)
 * - [io]      — Dispatchers.IO (off-thread I/O on Android/JVM; worker thread on Native)
 * - [default] — Dispatchers.Default (CPU-bound work)
 *
 * Registered as `single<AppDispatchers>` in [CoreModule] so the same instance is
 * reused throughout the app lifetime.
 */
class RealAppDispatchers : AppDispatchers {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.Default  // KMP: IO maps to Default on Native
    override val default: CoroutineDispatcher = Dispatchers.Default
}
