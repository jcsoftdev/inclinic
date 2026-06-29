package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme

/** Semantic variant for [ChipStatus]. */
enum class ChipStatusKind {
    Success, Warning, Error, Info, Neutral
}

/**
 * Resolves (textColor, backgroundColor) for a given [ChipStatusKind] from the palette.
 *
 * Extracted as a pure function so it can be unit-tested in commonTest without
 * a Compose runtime. Colors match the InClinic semantic palette.
 */
fun chipStatusColors(kind: ChipStatusKind, palette: AppColors): Pair<Color, Color> = when (kind) {
    ChipStatusKind.Success -> palette.green   to palette.greenBg
    ChipStatusKind.Warning -> palette.amber   to palette.amberBg
    ChipStatusKind.Error   -> palette.red     to palette.redBg
    ChipStatusKind.Info    -> palette.navy    to palette.navyTint
    ChipStatusKind.Neutral -> palette.muted   to palette.lav50
}

/**
 * Status chip atom matching the Pencil component/ChipStatus.
 *
 * Renders a pill-shaped label whose colors reflect the semantic [kind].
 * Read-only — no click handler, state lives in the caller.
 */
@Composable
fun ChipStatus(
    label: String,
    kind: ChipStatusKind,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val (textColor, bgColor) = chipStatusColors(kind, colors)

    Text(
        text       = label,
        color      = textColor,
        fontSize   = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier   = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}
