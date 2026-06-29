package com.inclinic.app.core.di

import com.inclinic.app.core.network.HttpClientEngineProvider
import com.inclinic.app.core.network.IosHttpClientEngineProvider
import com.inclinic.app.core.port.CardTokenizer
import com.inclinic.app.features.auth.infrastructure.local.createKeychainSettings
import com.inclinic.app.features.payment.MercadoPagoCardTokenizer
import com.russhwolf.settings.Settings
import org.koin.dsl.module

/**
 * iOS-specific Koin platform module.
 *
 * Provides:
 * - [HttpClientEngineProvider] → [IosHttpClientEngineProvider] (Darwin / NSURLSession)
 * - [Settings] → [createKeychainSettings] backed by iOS Keychain
 *
 * This module is loaded by [com.inclinic.app.di.KoinIos.initKoinIos] and must
 * NOT be loaded on Android.
 */
val platformIosModule = module {
    single<HttpClientEngineProvider> { IosHttpClientEngineProvider() }
    single<Settings> { createKeychainSettings() }
    single<CardTokenizer> { MercadoPagoCardTokenizer() }
}
