package com.inclinic.app.features.auth.infrastructure.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.Settings

/**
 * Creates an [EncryptedDataStoreSettings] instance for auth token storage.
 *
 * The DataStore is scoped to the provided [context] (use the Application context
 * from Koin to avoid leaking Activity references). A single DataStore per file name
 * is enforced by the [preferencesDataStore] delegate — do NOT create multiple
 * instances with the same file name.
 *
 * @param context Application context — required by DataStore.
 * @return [Settings] backed by DataStore + AES-256-GCM via Android Keystore.
 */
fun createAndroidEncryptedSettings(context: Context): Settings {
    return EncryptedDataStoreSettings(
        dataStore = context.authTokensDataStore,
        crypto = AndroidKeystoreAesGcm(),
    )
}

/** DataStore singleton delegate — one file per application. */
private val Context.authTokensDataStore by preferencesDataStore(name = "auth_tokens_encrypted")
