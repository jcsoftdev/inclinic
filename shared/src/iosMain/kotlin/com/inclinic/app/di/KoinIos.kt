package com.inclinic.app.di

import com.inclinic.app.core.di.platformIosModule

/**
 * iOS-specific Koin initializer.
 *
 * Call from Swift at app startup via [KoinHelper]:
 * ```swift
 * @main
 * struct iOSApp: App {
 *     init() { KoinHelper().initKoin() }
 *     var body: some Scene { WindowGroup { ContentView() } }
 * }
 * ```
 */
fun initKoinIos() {
    initKoin(extraPlatformModule = platformIosModule)
}
