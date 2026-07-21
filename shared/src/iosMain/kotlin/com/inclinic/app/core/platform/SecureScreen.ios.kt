package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable

/**
 * No-op on iOS for now. Blocking screen capture on iOS requires observing
 * `UIScreen.capturedDidChangeNotification` / overlaying a secure field and is
 * tracked separately; this keeps the common API available without a partial
 * implementation.
 */
@Composable
actual fun SecureScreen() {
    // Intentionally empty — see KDoc.
}
