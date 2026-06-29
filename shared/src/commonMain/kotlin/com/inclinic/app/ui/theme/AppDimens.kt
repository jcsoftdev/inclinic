package com.inclinic.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing, radius, and border-width constants for the InClinic design system.
 *
 * Always access via [AppTheme.dimens] inside a composition.
 */
@Immutable
data class AppDimens(
    // ── Corner radii ─────────────────────────────────────────────────────────
    val radiusChip: Dp,      // 8.dp  — chips / tags ($--r-chip)
    val radius: Dp,          // 10.dp — buttons / inputs ($--r-btn)
    val radiusMd: Dp,        // 12.dp — general radius ($--r)
    val radiusLarge: Dp,     // 16.dp — cards / sheets ($--r-card)
    val radiusXl: Dp,        // 20.dp — large containers ($--r-lg)
    val radiusPill: Dp,      // 36.dp — pill / nav pill ($--r-pill)

    // ── Border ───────────────────────────────────────────────────────────────
    val borderWidth: Dp,     // 1.5.dp

    // ── Input field ──────────────────────────────────────────────────────────
    val inputHeight: Dp,         // 52.dp
    val inputPaddingH: Dp,       // 14.dp
    val inputPaddingV: Dp,       // 14.dp
    val inputLabelBottomPad: Dp, // 6.dp

    // ── General spacing ───────────────────────────────────────────────────────
    val spacingXs: Dp,   // 4.dp
    val spacingSm: Dp,   // 8.dp
    val spacing12: Dp,   // 12.dp ($--gap-md)
    val spacingMd: Dp,   // 16.dp ($--gap-lg)
    val spacing20: Dp,   // 20.dp ($--gap-xl)
    val spacingLg: Dp,   // 24.dp
    val spacingXl: Dp,   // 32.dp
    val spacingXxl: Dp,  // 48.dp
)

val DefaultAppDimens = AppDimens(
    radiusChip           = 8.dp,
    radius               = 10.dp,
    radiusMd             = 12.dp,
    radiusLarge          = 16.dp,
    radiusXl             = 20.dp,
    radiusPill           = 36.dp,
    borderWidth          = 1.5.dp,
    inputHeight          = 52.dp,
    inputPaddingH        = 16.dp,
    inputPaddingV        = 14.dp,
    inputLabelBottomPad  = 6.dp,
    spacingXs            = 4.dp,
    spacingSm            = 8.dp,
    spacing12            = 12.dp,
    spacingMd            = 16.dp,
    spacing20            = 20.dp,
    spacingLg            = 24.dp,
    spacingXl            = 32.dp,
    spacingXxl           = 48.dp,
)
