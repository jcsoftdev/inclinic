package com.inclinic.app.ui.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Star
import com.inclinic.app.ui.theme.AppTheme

/**
 * Horizontal row of [max] star icons where the first [rating] are filled with
 * [AppTheme.colors.amber] and the rest use [AppTheme.colors.border].
 *
 * The root Row carries a content description "$rating of $max stars" for test
 * semantics and accessibility.
 *
 * @param rating  Number of filled stars. Clamped to [0, max] by the caller.
 * @param modifier  Modifier applied to the root [Row].
 * @param max  Total number of stars (default 5).
 * @param size  Diameter of each star icon (default 16.dp).
 */
@Composable
fun StarRating(
    rating: Int,
    modifier: Modifier = Modifier,
    max: Int = 5,
    size: Dp = 16.dp,
) {
    val colors = AppTheme.colors

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        modifier              = modifier.semantics {
            contentDescription = "$rating of $max stars"
        },
    ) {
        repeat(max) { index ->
            val filled = index < rating
            Icon(
                imageVector        = Lucide.Star,
                contentDescription = null,
                tint               = if (filled) colors.amber else colors.border,
                modifier           = Modifier.size(size),
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Composable
internal fun PreviewStarRatingLight() {
    AppTheme(useDarkTheme = false) {
        StarRating(rating = 3, max = 5)
    }
}

@Composable
internal fun PreviewStarRatingDark() {
    AppTheme(useDarkTheme = true) {
        StarRating(rating = 3, max = 5)
    }
}
