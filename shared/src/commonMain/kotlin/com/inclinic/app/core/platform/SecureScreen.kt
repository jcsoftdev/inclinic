package com.inclinic.app.core.platform

import androidx.compose.runtime.Composable

/**
 * Marks the current screen as security-sensitive while it is composed.
 *
 * On Android this sets `WindowManager.LayoutParams.FLAG_SECURE`, which blocks
 * screenshots, screen recording and the "recent apps" thumbnail for as long as
 * the calling composable is on screen; the flag is cleared on dispose so other
 * screens are unaffected.
 *
 * On iOS this is currently a no-op (the platform equivalent — hiding the window
 * on screen capture — is handled separately and is not yet bridged).
 *
 * Use on screens that render secrets or payment data (e.g. 2FA setup, card entry).
 */
@Composable
expect fun SecureScreen()
