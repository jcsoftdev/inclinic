package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme

// ── Pure helpers (unit-testable) ──────────────────────────────────────────────

enum class KpiTrend { Up, Down, Flat }

/** Returns the text color to use for a [KpiTrend] badge. Testable without Compose. */
fun kpiTrendColor(trend: KpiTrend?, palette: AppColors): Color = when (trend) {
    KpiTrend.Up   -> palette.green
    KpiTrend.Down -> palette.red
    KpiTrend.Flat -> palette.muted
    null          -> palette.muted
}

/** Returns a Unicode arrow/dash symbol representing a [KpiTrend]. Testable without Compose. */
fun kpiTrendIcon(trend: KpiTrend?): String = when (trend) {
    KpiTrend.Up   -> "↑"
    KpiTrend.Down -> "↓"
    KpiTrend.Flat -> "–"
    null          -> ""
}

// ── Composable ────────────────────────────────────────────────────────────────

/**
 * White metric card with a large value, a label below, and an optional trend badge.
 *
 * @param label   Metric name, shown below the value (e.g. "Pacientes hoy").
 * @param value   Formatted value string (e.g. "24", "S/ 1,200").
 * @param trend   Optional trend direction; renders a colored arrow and text color.
 * @param modifier Modifier forwarded to the card root.
 */
@Composable
fun KpiCard(
    label: String,
    value: String,
    trend: KpiTrend? = null,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val trendColor = kpiTrendColor(trend, colors)
    val trendIcon  = kpiTrendIcon(trend)

    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .background(colors.surface, AppTheme.shapes.large)
            .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text       = value,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = colors.text,
            )
            if (trend != null) {
                Spacer(Modifier.padding(start = 6.dp))
                Text(
                    text       = trendIcon,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = trendColor,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text  = label,
            style = AppTheme.typography.subtitle,
            color = colors.muted,
        )
    }
}
