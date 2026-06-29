package com.inclinic.app.core.concurrency

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Injected dispatcher container — fulfils LSP (Liskov Substitution Principle):
 * tests swap in TestCoroutineDispatcher without touching production code.
 *
 * Platform modules (androidMain / iosMain) provide concrete instances via Koin.
 * No expect/actual — DI handles dispatch.
 */
interface AppDispatchers {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
