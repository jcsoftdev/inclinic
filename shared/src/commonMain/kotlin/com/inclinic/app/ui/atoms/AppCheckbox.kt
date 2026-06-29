package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.inclinic.app.ui.theme.AppTheme

/**
 * Checkbox atom.
 *
 * 22.dp box with [AppTheme.shapes.small] (8.dp radius).
 * - **Checked**: navy background, centered white Lucide [Check] icon (14.dp), content description "checked".
 * - **Unchecked**: elevated background, 1.5.dp border, no icon.
 *
 * @param checked          Current state.
 * @param onCheckedChange  Callback with the negated value on click.
 * @param modifier         Modifier applied to the outer Box.
 * @param enabled          When false, click is suppressed.
 */
@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val shapes = AppTheme.shapes
    val dimens = AppTheme.dimens

    val shape = shapes.small

    Box(
        contentAlignment = Alignment.Center,
        modifier         = modifier
            .size(22.dp)
            .clip(shape)
            .background(if (checked) colors.navy else colors.elevated)
            .then(
                if (!checked) Modifier.border(dimens.borderWidth, colors.border, shape)
                else Modifier,
            )
            .testTag("AppCheckbox")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                enabled           = enabled,
                onClick           = { onCheckedChange(!checked) },
            ),
    ) {
        if (checked) {
            Icon(
                imageVector        = Lucide.Check,
                contentDescription = "checked",
                tint               = androidx.compose.ui.graphics.Color.White,
                modifier           = Modifier.size(14.dp),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Composable
internal fun PreviewAppCheckboxLight() {
    AppTheme(useDarkTheme = false) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            AppCheckbox(checked = false, onCheckedChange = {})
            AppCheckbox(checked = true, onCheckedChange = {})
        }
    }
}

@Composable
internal fun PreviewAppCheckboxDark() {
    AppTheme(useDarkTheme = true) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            AppCheckbox(checked = false, onCheckedChange = {})
            AppCheckbox(checked = true, onCheckedChange = {})
        }
    }
}
