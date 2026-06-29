package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.inclinic.app.ui.theme.AppTheme

/**
 * Full-size semi-transparent scrim with centered progress indicator.
 *
 * When [visible], intercepts all pointer input so the UI behind is non-interactive.
 * Renders nothing when [visible] is false.
 *
 * @param visible  Whether the overlay is shown.
 * @param modifier Modifier forwarded to the root Box (typically [Modifier.fillMaxSize]).
 */
@Composable
fun LoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!visible) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.25f))
            .pointerInput(Unit) { /* intercept and swallow all events */ },
    ) {
        CircularProgressIndicator(color = AppTheme.colors.navy)
    }
}
