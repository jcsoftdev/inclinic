package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

/**
 * Package row showing title, a visual session-progress strip, and price.
 *
 * Triangulation skipped: pure Composable, no extractable logic.
 *
 * @param title             Package or treatment name.
 * @param sessionsRemaining Number of sessions still available.
 * @param totalSessions     Total sessions in the package (denominator for the strip).
 * @param priceFormatted    Formatted price string (e.g. "S/ 350.00").
 * @param onClick           Click handler.
 */
@Composable
fun PackageRow(
    title: String,
    sessionsRemaining: Int,
    totalSessions: Int,
    priceFormatted: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors    = AppTheme.colors
    val dimens    = AppTheme.dimens
    val fraction  = if (totalSessions > 0) sessionsRemaining.toFloat() / totalSessions else 0f

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
        modifier            = modifier
            .fillMaxWidth()
            .background(colors.surface, AppTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.fillMaxWidth(),
        ) {
            Text(
                text       = title,
                style      = AppTheme.typography.body,
                fontWeight = FontWeight.Bold,
                color      = colors.text,
                modifier   = Modifier.weight(1f),
            )
            Text(
                text       = priceFormatted,
                style      = AppTheme.typography.body,
                fontWeight = FontWeight.Bold,
                color      = colors.navy,
            )
        }

        // Progress strip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(colors.lav50, AppTheme.shapes.pill),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(6.dp)
                    .background(colors.navy, AppTheme.shapes.pill),
            )
        }

        Text(
            text  = "$sessionsRemaining de $totalSessions sesiones restantes",
            style = AppTheme.typography.subtitle,
            color = colors.muted,
        )
    }
}
