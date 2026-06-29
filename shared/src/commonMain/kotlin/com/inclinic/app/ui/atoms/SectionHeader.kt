package com.inclinic.app.ui.atoms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Title + optional subtitle section header.
 *
 * Bottom spacing is intentionally omitted — callers control layout gaps via [Spacer].
 *
 * @param title    Primary heading text.
 * @param subtitle Optional secondary description below the title.
 * @param modifier Modifier forwarded to the root [Column].
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography

    Column(modifier = modifier) {
        Text(
            text  = title,
            style = typography.titleLarge,
            color = colors.text,
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text  = subtitle,
                style = typography.subtitle,
                color = colors.muted,
            )
        }
    }
}
