package com.inclinic.app.features.auth.infrastructure.local

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * [Settings] adapter backed by [DataStore] + AES-256-GCM via Android Keystore.
 *
 * ## Design decision: sync Settings interface vs async DataStore
 * The [com.russhwolf.settings.Settings] interface is synchronous, while DataStore is
 * entirely coroutine-based. We bridge the gap with [runBlocking] on the calling thread.
 *
 * **Tradeoff**: [runBlocking] blocks the calling thread. Since [TokenLocalDataSource]
 * wraps all calls in `suspend fun` (already dispatched on IO), this is acceptable —
 * the IO dispatcher has a thread pool and won't starve the main thread.
 *
 * **Future improvement**: The `multiplatform-settings-coroutines` artifact exposes
 * `SuspendSettings` — a coroutine-native variant. Migrating [TokenLocalDataSource]
 * to accept `SuspendSettings` (or a custom async port) would eliminate [runBlocking].
 * Tracked as tech debt; the current shape is sufficient for v1.
 *
 * ## String-only surface
 * Only [putString]/[getString]/[getStringOrNull] are implemented. Other primitive types
 * ([putInt], [putLong], etc.) throw [UnsupportedOperationException] — token storage
 * requires string values only.
 *
 * @param dataStore Injected DataStore instance (one per feature, provided by DI).
 * @param crypto    AES-GCM helper backed by Android Keystore.
 */
internal class EncryptedDataStoreSettings(
    private val dataStore: DataStore<Preferences>,
    private val crypto: AndroidKeystoreAesGcm = AndroidKeystoreAesGcm(),
) : Settings {

    // ── Write ────────────────────────────────────────────────────────────────

    override fun putString(key: String, value: String): Unit = runBlocking {
        val encrypted = crypto.encrypt(value.encodeToByteArray())
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        dataStore.edit { prefs -> prefs[stringPreferencesKey(key)] = encoded }
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    override fun getString(key: String, defaultValue: String): String =
        getStringOrNull(key) ?: defaultValue

    override fun getStringOrNull(key: String): String? = runBlocking {
        val prefs = dataStore.data.first()
        val encoded = prefs[stringPreferencesKey(key)] ?: return@runBlocking null
        try {
            val blob = Base64.decode(encoded, Base64.NO_WRAP)
            crypto.decrypt(blob).decodeToString()
        } catch (_: Exception) {
            // Corrupted entry — treat as absent.
            null
        }
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    override fun remove(key: String): Unit = runBlocking {
        dataStore.edit { prefs -> prefs.remove(stringPreferencesKey(key)) }
    }

    override fun clear(): Unit = runBlocking {
        dataStore.edit { prefs -> prefs.clear() }
    }

    // ── Existence / enumeration ───────────────────────────────────────────────

    override fun hasKey(key: String): Boolean = runBlocking {
        stringPreferencesKey(key) in dataStore.data.first()
    }

    override val keys: Set<String>
        get() = runBlocking {
            dataStore.data.first().asMap().keys.map { it.name }.toSet()
        }

    override val size: Int
        get() = runBlocking { dataStore.data.first().asMap().size }

    // ── Unsupported primitive types (v1: string-only storage) ────────────────

    override fun putInt(key: String, value: Int): Unit =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: putInt not supported — use putString")

    override fun getInt(key: String, defaultValue: Int): Int =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getInt not supported")

    override fun getIntOrNull(key: String): Int? =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getIntOrNull not supported")

    override fun putLong(key: String, value: Long): Unit =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: putLong not supported")

    override fun getLong(key: String, defaultValue: Long): Long =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getLong not supported")

    override fun getLongOrNull(key: String): Long? =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getLongOrNull not supported")

    override fun putFloat(key: String, value: Float): Unit =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: putFloat not supported")

    override fun getFloat(key: String, defaultValue: Float): Float =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getFloat not supported")

    override fun getFloatOrNull(key: String): Float? =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getFloatOrNull not supported")

    override fun putDouble(key: String, value: Double): Unit =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: putDouble not supported")

    override fun getDouble(key: String, defaultValue: Double): Double =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getDouble not supported")

    override fun getDoubleOrNull(key: String): Double? =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getDoubleOrNull not supported")

    override fun putBoolean(key: String, value: Boolean): Unit =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: putBoolean not supported")

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getBoolean not supported")

    override fun getBooleanOrNull(key: String): Boolean? =
        throw UnsupportedOperationException("EncryptedDataStoreSettings: getBooleanOrNull not supported")
}
