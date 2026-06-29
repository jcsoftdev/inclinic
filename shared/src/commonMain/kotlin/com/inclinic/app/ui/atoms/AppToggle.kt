package com.inclinic.app.ui.atoms

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Toggle switch atom.
 *
 * Track: 52×30 pill, navy (on) / light (off). White 24.dp knob with shadow.
 * Knob offset animates from 4.dp (off) to 24.dp (on).
 *
 * @param checked          Current state.
 * @param onCheckedChange  Callback with the new state.
 * @param modifier         Modifier applied to the track [Box].
 * @param enabled          When false, click is suppressed.
 */
@Composable
fun AppToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors    = AppTheme.colors
    val elevation = AppTheme.elevation

    val knobOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 4.dp,
        label       = "toggle-knob-offset",
    )

    val trackColor = if (checked) colors.navy else colors.light
    val trackShape = RoundedCornerShape(15.dp)

    Box(
        modifier = modifier
            .size(width = 52.dp, height = 30.dp)
            .clip(trackShape)
            .background(trackColor)
            .testTag("AppToggle")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                enabled           = enabled,
                onClick           = { onCheckedChange(!checked) },
            ),
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset, y = 3.dp)
                .size(24.dp)
                .shadow(elevation = elevation.low, shape = CircleShape, clip = false)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Composable
internal fun PreviewAppToggleLight() {
    AppTheme(useDarkTheme = false) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            AppToggle(checked = false, onCheckedChange = {})
            AppToggle(checked = true, onCheckedChange = {})
        }
    }
}

@Composable
internal fun PreviewAppToggleDark() {
    AppTheme(useDarkTheme = true) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalAlignment     = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            AppToggle(checked = false, onCheckedChange = {})
            AppToggle(checked = true, onCheckedChange = {})
        }
    }
}
