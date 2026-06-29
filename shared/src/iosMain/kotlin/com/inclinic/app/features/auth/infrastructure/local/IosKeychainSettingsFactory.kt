package com.inclinic.app.features.auth.infrastructure.local

import com.russhwolf.settings.KeychainSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.ExperimentalSettingsImplementation

/**
 * Provides a [Settings] instance backed by the iOS Keychain.
 *
 * [KeychainSettings] is part of the main `multiplatform-settings` artifact for iOS targets
 * (no separate `-keychain` artifact needed as of 1.3.0). It is annotated
 * [ExperimentalSettingsImplementation] — this is the correct opt-in for v1; the API is
 * production-stable but the author reserves the right to make breaking changes.
 * Confine the opt-in to this factory only (do NOT propagate to callers).
 *
 * The `service` parameter corresponds to the Keychain service name / access group.
 * Using a distinct, app-specific value prevents key collisions with other apps or
 * other features within the same app.
 */
@OptIn(ExperimentalSettingsImplementation::class)
fun createKeychainSettings(service: String = "com.inclinic.app.auth"): Settings =
    KeychainSettings(service = service)
