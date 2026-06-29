package com.inclinic.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * InClinic design-token color palette — aligned 1:1 with the ClinicAI `design.pen`
 * variable set (themes: `dark` / `light`).
 *
 * Immutable data class so Compose stability inference works correctly.
 * Always access via [AppTheme.colors] inside a composition.
 */
@Immutable
data class AppColors(
    // ── Scaffold / surface ──────────────────────────────────────────────────
    val sand: Color,          // scaffold bg ($--sand)
    val base: Color,          // app base bg ($--base)
    val surface: Color,       // surface bg ($--surface)
    val elevated: Color,      // elevated card/sheet bg ($--elevated)

    // ── Border ──────────────────────────────────────────────────────────────
    val border: Color,        // default border ($--border)

    // ── Text ────────────────────────────────────────────────────────────────
    val text: Color,          // body text + headings ($--text)
    val muted: Color,         // secondary text + labels ($--muted)
    val light: Color,         // placeholders + disabled ($--light)

    // ── Brand / Primary ─────────────────────────────────────────────────────
    val navy: Color,          // primary ($--navy)
    val navyDark: Color,      // primary hover / pressed ($--navy-dark)
    val navyLight: Color,     // input focus border ($--navy-light)
    val navyTint: Color,      // navy tint bg ($--navy-tint)

    // ── Lavender ────────────────────────────────────────────────────────────
    val lav: Color,           // ($--lav)
    val lavLight: Color,      // ($--lav-light)
    val lav50: Color,         // input bg (code-only, not in .pen)

    // ── Accent ──────────────────────────────────────────────────────────────
    val purple: Color,        // ($--purple)
    val purpleBg: Color,      // ($--purple-bg)

    // ── Semantic: error / red ───────────────────────────────────────────────
    val red: Color,           // ($--red == $--error)
    val redBg: Color,         // ($--red-bg)
    val error: Color,         // ($--error)
    val errorBg: Color,       // ($--error-bg)
    val errorBorder: Color,   // ($--error-border)

    // ── Semantic: success / green ───────────────────────────────────────────
    val green: Color,         // ($--green)
    val greenBg: Color,       // ($--green-bg)
    val success: Color,       // ($--success)
    val successBg: Color,     // ($--success-bg)
    val successBorder: Color, // ($--success-border)

    // ── Semantic: warning / amber ───────────────────────────────────────────
    val amber: Color,         // ($--amber)
    val amberBg: Color,       // ($--amber-bg)
    val warning: Color,       // ($--warning)
    val warningBg: Color,     // ($--warning-bg)
    val warningBorder: Color, // ($--warning-border)

    // ── Semantic: info / blue ───────────────────────────────────────────────
    val blue: Color,          // ($--blue == $--info)
    val blueBg: Color,        // ($--blue-bg)
    val info: Color,          // ($--info)
    val infoBg: Color,        // ($--info-bg)
    val infoBorder: Color,    // ($--info-border)

    // ── Teal ────────────────────────────────────────────────────────────────
    val teal: Color,          // ($--teal)
    val tealBg: Color,        // ($--teal-bg)

    /** True when this set is the dark theme — lets callers branch without a CompositionLocal. */
    val isDark: Boolean,
)

/** Light color set — `design.pen` `mode: light`. */
val LightAppColors = AppColors(
    sand          = Color(0xFFF7F8FC),
    base          = Color(0xFFF2F4FB),
    surface       = Color(0xFFFFFFFF),
    elevated      = Color(0xFFFFFFFF),
    border        = Color(0xFFDDE1F0),
    text          = Color(0xFF1A1E35),
    muted         = Color(0xFF6B7280),
    light         = Color(0xFF9CA3AF),
    navy          = Color(0xFF2C3E7A),
    navyDark      = Color(0xFF1E2D5E),
    navyLight     = Color(0xFF4A5FA8),
    navyTint      = Color(0xFFE8EBF8),
    lav           = Color(0xFF8892C8),
    lavLight      = Color(0xFFBCC4E8),
    lav50         = Color(0xFFEEF0F9),
    purple        = Color(0xFF7C3AED),
    purpleBg      = Color(0xFFEDE9FE),
    red           = Color(0xFFDC2626),
    redBg         = Color(0xFFFEE2E2),
    error         = Color(0xFFDC2626),
    errorBg       = Color(0xFFFEE2E2),
    errorBorder   = Color(0xFFFCA5A5),
    green         = Color(0xFF16A34A),
    greenBg       = Color(0xFFDCFCE7),
    success       = Color(0xFF15803D),
    successBg     = Color(0xFFDCFCE7),
    successBorder = Color(0xFF86EFAC),
    amber         = Color(0xFFD97706),
    amberBg       = Color(0xFFFEF3C7),
    warning       = Color(0xFFB45309),
    warningBg     = Color(0xFFFEF3C7),
    warningBorder = Color(0xFFFCD34D),
    blue          = Color(0xFF1D6FA8),
    blueBg        = Color(0xFFDBEAFE),
    info          = Color(0xFF1D6FA8),
    infoBg        = Color(0xFFDBEAFE),
    infoBorder    = Color(0xFF93C5FD),
    teal          = Color(0xFF0F766E),
    tealBg        = Color(0xFFCCFBF1),
    isDark        = false,
)

/** Dark color set — `design.pen` `mode: dark`. */
val DarkAppColors = AppColors(
    sand          = Color(0xFF0A0B14),
    base          = Color(0xFF0A0B14),
    surface       = Color(0xFF12141F),
    elevated      = Color(0xFF1A1D2B),
    border        = Color(0xFF262A3D),
    text          = Color(0xFFEDEFFF),
    muted         = Color(0xFFA2A8C8),
    light         = Color(0xFF5A5F78),
    navy          = Color(0xFF5B6CFF),
    navyDark      = Color(0xFF4453D6),
    navyLight     = Color(0xFF8892C8),
    navyTint      = Color(0xFF1A1D2B),
    lav           = Color(0xFF8892C8),
    lavLight      = Color(0xFFA8B0E8),
    lav50         = Color(0xFF1A1D2B),
    purple        = Color(0xFFA78BFA),
    purpleBg      = Color(0xFF2A1E4D),
    red           = Color(0xFFFB5E6B),
    redBg         = Color(0xFF3A1A1F),
    error         = Color(0xFFFB5E6B),
    errorBg       = Color(0xFF3A1A1F),
    errorBorder   = Color(0xFF7A2F38),
    green         = Color(0xFF34D399),
    greenBg       = Color(0xFF14352A),
    success       = Color(0xFF34D399),
    successBg     = Color(0xFF14352A),
    successBorder = Color(0xFF2F7D5B),
    amber         = Color(0xFFFBBF24),
    amberBg       = Color(0xFF3A2E12),
    warning       = Color(0xFFFBBF24),
    warningBg     = Color(0xFF3A2E12),
    warningBorder = Color(0xFF7A5C1A),
    blue          = Color(0xFF54B8FF),
    blueBg        = Color(0xFF12304A),
    info          = Color(0xFF54B8FF),
    infoBg        = Color(0xFF12304A),
    infoBorder    = Color(0xFF2F5C7A),
    teal          = Color(0xFF2DD4BF),
    tealBg        = Color(0xFF0E3A35),
    isDark        = true,
)
