package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

// ── Model ─────────────────────────────────────────────────────────────────────

data class IncomeBar(val label: String, val amount: Double)

// ── Pure helper (unit-testable) ───────────────────────────────────────────────

/**
 * Normalizes a list of [IncomeBar] amounts to the [0f..1f] range.
 * - Empty list returns empty.
 * - Single bar or all-equal bars return [1f] for each entry.
 * - Zero-amount bar returns 0f when a non-zero max exists.
 */
fun normalizeBars(bars: List<IncomeBar>): List<Float> {
    if (bars.isEmpty()) return emptyList()
    val max = bars.maxOf { it.amount }
    return if (max == 0.0) {
        bars.map { 1f }
    } else {
        bars.map { (it.amount / max).toFloat() }
    }
}

// ── Composable ────────────────────────────────────────────────────────────────

/**
 * Simple vertical bar chart for income data.
 *
 * @param bars     Income data points with label and amount.
 * @param modifier Modifier forwarded to the root row.
 */
@Composable
fun IncomeChart(
    bars: List<IncomeBar>,
    modifier: Modifier = Modifier,
) {
    val colors     = AppTheme.colors
    val normalized = normalizeBars(bars)
    val chartHeight = 120.dp

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment     = Alignment.Bottom,
        modifier              = modifier
            .fillMaxWidth()
            .height(chartHeight + 24.dp)
            .padding(horizontal = 8.dp),
    ) {
        bars.forEachIndexed { index, bar ->
            val fraction = normalized.getOrElse(index) { 0f }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier            = Modifier
                    .weight(1f)
                    .height(chartHeight + 24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(chartHeight * fraction)
                        .background(colors.navy, AppTheme.shapes.small),
                )
                Text(
                    text      = bar.label,
                    style     = AppTheme.typography.caption,
                    color     = colors.muted,
                    modifier  = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
