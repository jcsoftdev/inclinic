package com.inclinic.app.ui.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.inclinic.app.ui.theme.AppTheme

/**
 * Price row with a label on the left and the formatted price on the right,
 * followed by a thin divider.
 *
 * Triangulation skipped: pure Composable, no extractable logic.
 *
 * @param label          Service or item description.
 * @param priceFormatted Pre-formatted price string (e.g. "S/ 120.00").
 * @param modifier       Modifier forwarded to the root column.
 */
@Composable
fun PriceRow(
    label: String,
    priceFormatted: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier
                .fillMaxWidth()
                .padding(vertical = dimens.spacingMd),
        ) {
            Text(
                text     = label,
                style    = AppTheme.typography.body,
                color    = colors.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text       = priceFormatted,
                style      = AppTheme.typography.body,
                fontWeight = FontWeight.Bold,
                color      = colors.navy,
            )
        }
        HorizontalDivider(color = colors.border, thickness = dimens.borderWidth)
    }
}
