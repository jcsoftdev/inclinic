package com.inclinic.app.core.secure

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class SettingsSecureStorage(private val settings: Settings) : SecureStorage {
    override suspend fun putString(key: String, value: String) {
        settings[key] = value
    }

    override suspend fun getString(key: String): String? =
        settings.getStringOrNull(key)

    override suspend fun remove(key: String) {
        settings.remove(key)
    }

    override suspend fun clearAll() {
        settings.clear()
    }
}
