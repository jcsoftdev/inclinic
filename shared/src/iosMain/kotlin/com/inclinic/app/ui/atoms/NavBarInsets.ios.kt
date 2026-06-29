package com.inclinic.app.ui.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// iOS skips the full home-indicator inset (~34pt) — that leaves a large empty
// gap. A modest fixed margin floats the pill near the bottom (modern iOS look)
// while still clearing the indicator.
@Composable
actual fun Modifier.navBarBottomSafePadding(): Modifier =
    this.padding(bottom = 20.dp)
