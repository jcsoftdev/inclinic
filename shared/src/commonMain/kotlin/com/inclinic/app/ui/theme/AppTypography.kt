package com.inclinic.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import inclinic.shared.generated.resources.Res
import inclinic.shared.generated.resources.funnel_sans_bold
import inclinic.shared.generated.resources.funnel_sans_regular
import inclinic.shared.generated.resources.funnel_sans_semibold
import inclinic.shared.generated.resources.inter_bold
import inclinic.shared.generated.resources.inter_italic
import inclinic.shared.generated.resources.inter_medium
import inclinic.shared.generated.resources.inter_regular
import inclinic.shared.generated.resources.inter_semibold
import org.jetbrains.compose.resources.Font

/** Sans family — `$--font-sans` = Inter. Body, labels, buttons, captions. */
@Composable
fun interFamily() = FontFamily(
    Font(Res.font.inter_regular,  FontWeight.Normal,   FontStyle.Normal),
    Font(Res.font.inter_medium,   FontWeight.Medium,   FontStyle.Normal),
    Font(Res.font.inter_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.inter_bold,     FontWeight.Bold,     FontStyle.Normal),
    Font(Res.font.inter_italic,   FontWeight.Normal,   FontStyle.Italic),
)

/** Display family — `$--font-display` = Funnel Sans. Hero + section titles. */
@Composable
fun funnelSansFamily() = FontFamily(
    Font(Res.font.funnel_sans_regular,  FontWeight.Normal,   FontStyle.Normal),
    Font(Res.font.funnel_sans_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.funnel_sans_bold,     FontWeight.Bold,     FontStyle.Normal),
)

@Immutable
data class AppTypography(
    val displayLarge:  TextStyle,   // Funnel Sans 36sp bold — Login hero
    val displayMedium: TextStyle,   // Funnel Sans 28sp bold — Register title
    val displaySmall:  TextStyle,   // Funnel Sans 26sp bold — ForgotPassword title
    val displayXSmall: TextStyle,   // Funnel Sans 24sp bold — Reset title
    val displayNano:   TextStyle,   // Funnel Sans 22sp bold — Onboarding header
    val titleLarge:    TextStyle,   // Funnel Sans 20sp bold — Onboarding step title
    val buttonLg:      TextStyle,   // Inter 15sp semibold — primary button
    val body:          TextStyle,   // Inter 14sp normal — body copy
    val subtitle:      TextStyle,   // Inter 13sp normal — secondary text
    val link:          TextStyle,   // Inter 13sp semibold — link text
    val label:         TextStyle,   // Inter 12sp semibold 0.4sp — field label
    val fieldError:    TextStyle,   // Inter 12sp normal — error helper
    val caption:       TextStyle,   // Inter 12sp semibold 0.6sp — step counter
)

@Composable
fun buildAppTypography(): AppTypography {
    val sans    = interFamily()
    val display = funnelSansFamily()
    return AppTypography(
        displayLarge = TextStyle(fontFamily = display, fontSize = 36.sp, fontWeight = FontWeight.Bold),
        displayMedium = TextStyle(fontFamily = display, fontSize = 28.sp, fontWeight = FontWeight.Bold),
        displaySmall = TextStyle(fontFamily = display, fontSize = 26.sp, fontWeight = FontWeight.Bold),
        displayXSmall = TextStyle(fontFamily = display, fontSize = 24.sp, fontWeight = FontWeight.Bold),
        displayNano = TextStyle(fontFamily = display, fontSize = 22.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontFamily = display, fontSize = 20.sp, fontWeight = FontWeight.Bold),
        buttonLg = TextStyle(fontFamily = sans, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
        body = TextStyle(fontFamily = sans, fontSize = 14.sp, fontWeight = FontWeight.Normal),
        subtitle = TextStyle(fontFamily = sans, fontSize = 13.sp, fontWeight = FontWeight.Normal),
        link = TextStyle(fontFamily = sans, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
        label = TextStyle(fontFamily = sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
        fieldError = TextStyle(fontFamily = sans, fontSize = 12.sp, fontWeight = FontWeight.Normal),
        caption = TextStyle(fontFamily = sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
    )
}

// Fallback con system fonts — solo usado si AppTheme no está en el árbol
val DefaultAppTypography = AppTypography(
    displayLarge  = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 36.sp, fontWeight = FontWeight.Bold),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 28.sp, fontWeight = FontWeight.Bold),
    displaySmall  = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 26.sp, fontWeight = FontWeight.Bold),
    displayXSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 24.sp, fontWeight = FontWeight.Bold),
    displayNano   = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge    = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 20.sp, fontWeight = FontWeight.Bold),
    buttonLg      = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    body          = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, fontWeight = FontWeight.Normal),
    subtitle      = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp, fontWeight = FontWeight.Normal),
    link          = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    label         = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp),
    fieldError    = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Normal),
    caption       = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.6.sp),
)
