package com.inclinic.app.ui.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.inclinic.app.ui.theme.AppTheme

/**
 * Text-button link atom.
 *
 * @param text       Label to display.
 * @param emphasized When true: navy + bold. When false: muted + normal weight.
 * @param onClick    Click handler.
 * @param modifier   Modifier forwarded to the [Text].
 */
@Composable
fun AppLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    val color      = if (emphasized) colors.navy else colors.muted
    val fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Normal

    Text(
        text  = text,
        style = typography.link.copy(
            color          = color,
            fontWeight     = fontWeight,
            textDecoration = TextDecoration.None,
        ),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication        = null,
            onClick           = onClick,
        ),
    )
}
