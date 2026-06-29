package com.inclinic.app.di

import android.app.Application
import com.inclinic.app.core.di.platformAndroidModule
import org.koin.android.ext.koin.androidContext

/**
 * Android-specific Koin initializer.
 *
 * Call from [com.inclinic.app.MainApplication.onCreate]:
 * ```
 * class MainApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         initKoinAndroid(this)
 *     }
 * }
 * ```
 *
 * This registers the Application [android.content.Context] in the Koin graph so that
 * [platformAndroidModule] can resolve it via `androidContext()` when creating [Settings].
 */
fun initKoinAndroid(application: Application) = initKoin(
    extraPlatformModule = platformAndroidModule,
    appDeclaration = { androidContext(application) },
)
