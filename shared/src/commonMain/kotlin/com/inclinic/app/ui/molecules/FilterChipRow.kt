package com.inclinic.app.ui.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

// ── Pure helper (unit-testable) ───────────────────────────────────────────────

/** Returns true when [option] exactly matches [selected] (case-sensitive). */
fun isChipSelected(option: String, selected: String): Boolean = option == selected

// ── Composable ────────────────────────────────────────────────────────────────

/**
 * Horizontally scrollable row of filter chips.
 *
 * @param options  List of option strings to display as chips.
 * @param selected Currently selected option string.
 * @param onSelect Callback invoked when a chip is tapped.
 * @param modifier Modifier forwarded to the row.
 */
@Composable
fun FilterChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        modifier              = modifier.horizontalScroll(rememberScrollState()),
    ) {
        options.forEach { option ->
            val active = isChipSelected(option, selected)
            val bg     = if (active) colors.navy      else colors.lav50
            val fg     = if (active) Color.White      else colors.muted

            Text(
                text       = option,
                style      = AppTheme.typography.label,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color      = fg,
                modifier   = Modifier
                    .background(bg, AppTheme.shapes.pill)
                    .clickable { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            )
        }
    }
}
