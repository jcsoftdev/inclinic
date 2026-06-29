package com.inclinic.app.ui.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Bottom safe-area padding for the floating nav bar.
 *
 * Android applies the real `navigationBars` inset (gesture pill or 3-button nav
 * height) so the bar always clears the system navigation. iOS deliberately skips
 * the home-indicator inset (~34pt): clearing it would push the floating pill far
 * up and leave a large empty gap, so on iOS the pill hugs the bottom the same way
 * Android's does.
 */
@Composable
expect fun Modifier.navBarBottomSafePadding(): Modifier
