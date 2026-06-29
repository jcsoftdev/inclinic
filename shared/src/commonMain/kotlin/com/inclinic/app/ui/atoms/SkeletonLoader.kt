package com.inclinic.app.ui.atoms

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inclinic.app.ui.theme.AppTheme

// ── Shimmer helper ────────────────────────────────────────────────────────────

/**
 * Returns (shimmerBrush, baseColor) resolved from the current palette.
 *
 * Extracted as a pure function so the color-resolution logic can be tested
 * in commonTest without a Compose runtime — mirrors the [chipStatusColors]
 * pattern from ChipStatus.
 */
fun skeletonColors(
    palette: com.inclinic.app.ui.theme.AppColors,
): Pair<Color, Color> = palette.elevated to palette.border.copy(alpha = 0.55f)

// ── Core atom ─────────────────────────────────────────────────────────────────

/**
 * Placeholder shimmer block — matches the `component/SkeletonLoader` Pencil node (ptU8r).
 *
 * Renders a single rounded rectangle that animates a left-to-right shimmer
 * using [AppTheme.colors.elevated] as the base and [AppTheme.colors.border]
 * for the highlight sweep. No semantic meaning — purely a loading placeholder.
 *
 * @param modifier      Modifier forwarded to the outer Box.
 * @param shape         Corner shape (default: 12.dp radius, matching Pencil ptU8r).
 * @param animate       When true, runs the shimmer animation. Pass false for
 *                      snapshot tests or reduced-motion scenarios.
 */
@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    animate: Boolean = true,
) {
    val colors = AppTheme.colors
    val (base, highlight) = skeletonColors(colors)

    val shimmerBrush = if (animate) {
        val transition = rememberInfiniteTransition(label = "skeleton-shimmer")
        val offset by transition.animateFloat(
            initialValue = -300f,
            targetValue  = 1500f,
            animationSpec = infiniteRepeatable(
                animation  = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "skeleton-offset",
        )
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start  = Offset(offset, 0f),
            end    = Offset(offset + 300f, 0f),
        )
    } else {
        Brush.linearGradient(colors = listOf(base, base))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush),
    )
}

// ── Convenience helpers ───────────────────────────────────────────────────────

/**
 * A vertical stack of [count] skeleton rows shaped to resemble a list item.
 *
 * Each row is 72dp tall with 8dp gap — matching the Pencil ptU8r layout
 * (two 72 × 350dp rectangles, gap 8).
 *
 * @param count     Number of placeholder rows (default 3).
 * @param modifier  Modifier forwarded to the outer Column.
 * @param animate   Forwarded to each [SkeletonLoader] row.
 */
@Composable
fun SkeletonListRows(
    count: Int = 3,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        repeat(count) {
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                animate  = animate,
            )
        }
    }
}

/**
 * A single skeleton row that mimics a conversation / message list item:
 * avatar circle on the left, two text-line placeholders on the right.
 *
 * Matches the visual rhythm of ConversationRow / NotificationRow.
 *
 * @param modifier Modifier forwarded to the outer Row.
 * @param animate  Forwarded to each [SkeletonLoader] block.
 */
@Composable
fun SkeletonMessageRow(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Avatar placeholder
        SkeletonLoader(
            modifier = Modifier.size(44.dp),
            shape    = CircleShape,
            animate  = animate,
        )

        Spacer(Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            // Name line
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(14.dp),
                shape   = RoundedCornerShape(6.dp),
                animate = animate,
            )
            // Message line
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(12.dp),
                shape   = RoundedCornerShape(6.dp),
                animate = animate,
            )
        }
    }
}

/**
 * A single skeleton row that mimics a notification list item:
 * icon circle on the left, title + body lines on the right.
 *
 * Matches the visual rhythm of [com.inclinic.app.ui.molecules.NotificationRow].
 *
 * @param modifier Modifier forwarded to the outer Row.
 * @param animate  Forwarded to each [SkeletonLoader] block.
 */
@Composable
fun SkeletonNotificationRow(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Icon circle placeholder
        SkeletonLoader(
            modifier = Modifier.size(40.dp),
            shape    = CircleShape,
            animate  = animate,
        )

        Spacer(Modifier.width(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f),
        ) {
            // Title line
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth(0.60f)
                    .height(14.dp),
                shape   = RoundedCornerShape(6.dp),
                animate = animate,
            )
            // Body line
            SkeletonLoader(
                modifier = Modifier
                    .fillMaxWidth(0.90f)
                    .height(12.dp),
                shape   = RoundedCornerShape(6.dp),
                animate = animate,
            )
        }
    }
}

/**
 * A single skeleton row that mimics a package card:
 * a taller block with two internal lines, matching PackageCard.
 *
 * @param modifier Modifier forwarded to the outer Column.
 * @param animate  Forwarded to each [SkeletonLoader] block.
 */
@Composable
fun SkeletonPackageRow(
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val shape = RoundedCornerShape(16.dp)
    val colors = AppTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .padding(16.dp),
    ) {
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(16.dp),
            shape   = RoundedCornerShape(6.dp),
            animate = animate,
        )
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth(0.40f)
                .height(12.dp),
            shape   = RoundedCornerShape(6.dp),
            animate = animate,
        )
        Spacer(Modifier.height(4.dp))
        SkeletonLoader(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            shape   = RoundedCornerShape(4.dp),
            animate = animate,
        )
    }
}
