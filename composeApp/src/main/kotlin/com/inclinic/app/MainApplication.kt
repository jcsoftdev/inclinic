package com.inclinic.app

import android.app.Application
import com.inclinic.app.di.initKoinAndroid

/**
 * Android Application entry point.
 *
 * Initializes the Koin DI graph at app startup before any Activity is created.
 * [initKoinAndroid] registers the Application context so that [platformAndroidModule]
 * can create the encrypted DataStore-backed [Settings] via [androidContext()].
 *
 * **Important**: Register this class in [AndroidManifest.xml] via:
 * ```xml
 * <application android:name="com.inclinic.app.MainApplication" ...>
 * ```
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoinAndroid(this)
    }
}
