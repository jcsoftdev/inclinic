package com.inclinic.app.di

/**
 * Swift-callable Koin initialization helper.
 *
 * Swift interop: The Kotlin compiler exports this as `KoinHelper` in the Obj-C header.
 * Swift calls it as:
 * ```swift
 * KoinHelper().initKoin()
 * ```
 *
 * Note: `object` is preferred here so Swift can call it without instantiation:
 * ```swift
 * KoinHelperKt.KoinHelper.shared.initKoin()  // singleton access
 * // OR (with SKIE):
 * KoinHelper.shared.initKoin()
 * ```
 *
 * Without SKIE, the companion-style access is:
 * ```swift
 * // Swift sees it as a standard Obj-C class (generated name: KoinHelper)
 * KoinHelper().initKoin()
 * ```
 *
 * The simplest approach that works without SKIE is using `object` —
 * Swift receives it as a singleton and calls `KoinHelperKt.KoinHelper.shared.initKoin()`.
 * For clarity, iOS callers should use `KoinHelper.shared.initKoin()` (Obj-C compat).
 */
object KoinHelper {
    fun start() {
        initKoinIos()
    }
}
