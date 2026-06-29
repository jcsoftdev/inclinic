package com.inclinic.app.core.di

import android.content.Context
import com.inclinic.app.core.network.AndroidHttpClientEngineProvider
import com.inclinic.app.core.network.HttpClientEngineProvider
import com.inclinic.app.core.port.CardTokenizer
import com.inclinic.app.features.auth.infrastructure.local.createAndroidEncryptedSettings
import com.inclinic.app.features.payment.MercadoPagoCardTokenizer
import com.russhwolf.settings.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin platform module.
 *
 * Provides:
 * - [HttpClientEngineProvider] → [AndroidHttpClientEngineProvider] (OkHttp)
 * - [Settings] → [createAndroidEncryptedSettings] backed by DataStore + AES-256-GCM
 *
 * This module is loaded by [com.inclinic.app.di.KoinAndroid.initKoinAndroid] and must
 * NOT be loaded on iOS (Darwin engine and KeychainSettings are provided by the iOS module).
 */
val platformAndroidModule = module {
    single<HttpClientEngineProvider> { AndroidHttpClientEngineProvider() }
    single<Settings> {
        val context: Context = androidContext()
        createAndroidEncryptedSettings(context)
    }
    single<CardTokenizer> { MercadoPagoCardTokenizer(publicKey = "TEST-mp-public-key") }
}
