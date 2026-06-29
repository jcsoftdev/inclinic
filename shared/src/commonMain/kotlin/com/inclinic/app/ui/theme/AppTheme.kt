package com.inclinic.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

// ── CompositionLocals ─────────────────────────────────────────────────────────

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("AppColors not provided — wrap your tree with AppTheme { … }")
}

val LocalAppDimens = staticCompositionLocalOf<AppDimens> {
    error("AppDimens not provided — wrap your tree with AppTheme { … }")
}

val LocalAppTypography = staticCompositionLocalOf<AppTypography> {
    error("AppTypography not provided — wrap your tree with AppTheme { … }")
}

val LocalAppShapes = staticCompositionLocalOf<AppShapes> {
    error("AppShapes not provided — wrap your tree with AppTheme { … }")
}

val LocalAppOpacity = staticCompositionLocalOf<AppOpacity> {
    error("AppOpacity not provided — wrap your tree with AppTheme { … }")
}

val LocalAppMotion = staticCompositionLocalOf<AppMotion> {
    error("AppMotion not provided — wrap your tree with AppTheme { … }")
}

val LocalAppElevation = staticCompositionLocalOf<AppElevation> {
    error("AppElevation not provided — wrap your tree with AppTheme { … }")
}

// ── Theme wrapper ─────────────────────────────────────────────────────────────

/**
 * Root theme wrapper for the InClinic design system.
 *
 * Provides [AppColors], [AppDimens], and [AppTypography] via CompositionLocals,
 * and wraps [MaterialTheme] with a derived [ColorScheme] so that Material3
 * components (buttons, progress indicators, etc.) automatically pick up the
 * InClinic brand colors.
 *
 * Usage:
 * ```kotlin
 * AppTheme {
 *     Surface(color = AppTheme.colors.sand) { … }
 * }
 * ```
 */
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    colors: AppColors = if (useDarkTheme) DarkAppColors else LightAppColors,
    dimens: AppDimens = DefaultAppDimens,
    typography: AppTypography = buildAppTypography(),
    shapes: AppShapes = DefaultAppShapes,
    opacity: AppOpacity = DefaultAppOpacity,
    motion: AppMotion = DefaultAppMotion,
    elevation: AppElevation = DefaultAppElevation,
    content: @Composable () -> Unit,
) {
    val white = androidx.compose.ui.graphics.Color.White
    val colorScheme: ColorScheme = if (colors.isDark) {
        darkColorScheme(
            primary            = colors.navy,
            onPrimary          = white,
            primaryContainer   = colors.navyLight,
            onPrimaryContainer = colors.text,
            inversePrimary     = colors.navyDark,
            secondary          = colors.lav,
            onSecondary        = white,
            secondaryContainer = colors.lav50,
            onSecondaryContainer = colors.text,
            tertiary           = colors.teal,
            onTertiary         = white,
            tertiaryContainer  = colors.tealBg,
            background         = colors.sand,
            onBackground       = colors.text,
            surface            = colors.surface,
            onSurface          = colors.text,
            surfaceVariant     = colors.lav50,
            onSurfaceVariant   = colors.muted,
            error              = colors.error,
            onError            = white,
            errorContainer     = colors.errorBg,
            onErrorContainer   = colors.error,
            outline            = colors.border,
            outlineVariant     = colors.border,
        )
    } else {
        lightColorScheme(
            primary            = colors.navy,
            onPrimary          = white,
            primaryContainer   = colors.navyLight,
            onPrimaryContainer = colors.text,
            inversePrimary     = colors.navyDark,
            secondary          = colors.lav,
            onSecondary        = white,
            secondaryContainer = colors.lav50,
            onSecondaryContainer = colors.text,
            tertiary           = colors.teal,
            onTertiary         = white,
            tertiaryContainer  = colors.tealBg,
            background         = colors.sand,
            onBackground       = colors.text,
            surface            = colors.surface,
            onSurface          = colors.text,
            surfaceVariant     = colors.lav50,
            onSurfaceVariant   = colors.muted,
            error              = colors.error,
            onError            = white,
            errorContainer     = colors.errorBg,
            onErrorContainer   = colors.error,
            outline            = colors.border,
            outlineVariant     = colors.border,
        )
    }

    CompositionLocalProvider(
        LocalAppColors     provides colors,
        LocalAppDimens     provides dimens,
        LocalAppTypography provides typography,
        LocalAppShapes     provides shapes,
        LocalAppOpacity    provides opacity,
        LocalAppMotion     provides motion,
        LocalAppElevation  provides elevation,
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}

// ── Static accessor object ────────────────────────────────────────────────────

/**
 * Convenience accessor for InClinic design tokens inside a composition.
 *
 * ```kotlin
 * val navy = AppTheme.colors.navy
 * val r    = AppTheme.dimens.radius
 * ```
 */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable
        get() = LocalAppColors.current

    val dimens: AppDimens
        @Composable @ReadOnlyComposable
        get() = LocalAppDimens.current

    val typography: AppTypography
        @Composable @ReadOnlyComposable
        get() = LocalAppTypography.current

    val shapes: AppShapes
        @Composable @ReadOnlyComposable
        get() = LocalAppShapes.current

    val opacity: AppOpacity
        @Composable @ReadOnlyComposable
        get() = LocalAppOpacity.current

    val motion: AppMotion
        @Composable @ReadOnlyComposable
        get() = LocalAppMotion.current

    val elevation: AppElevation
        @Composable @ReadOnlyComposable
        get() = LocalAppElevation.current
}
