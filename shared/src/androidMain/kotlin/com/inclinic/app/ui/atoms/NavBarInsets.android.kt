package com.inclinic.app.ui.atoms

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Clears the system navigation (gesture pill OR 3-button bar) via the real
// inset, then adds a small, consistent gap above it.
@Composable
actual fun Modifier.navBarBottomSafePadding(): Modifier =
    this
        .windowInsetsPadding(WindowInsets.navigationBars)
        .padding(bottom = 12.dp)
