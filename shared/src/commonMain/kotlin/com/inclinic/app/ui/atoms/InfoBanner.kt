package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TriangleAlert
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme

/** Semantic tone for [InfoBanner]. */
enum class InfoBannerTone { Info, Success, Warning, Error }

/**
 * Resolves (background, foreground) colors for a given [InfoBannerTone] from the palette.
 *
 * Extracted as a pure function for unit-testability without a Compose runtime.
 */
internal fun infoBannerColors(tone: InfoBannerTone, palette: AppColors): Pair<Color, Color> =
    when (tone) {
        InfoBannerTone.Info    -> palette.infoBg    to palette.info
        InfoBannerTone.Success -> palette.successBg to palette.success
        InfoBannerTone.Warning -> palette.warningBg to palette.warning
        InfoBannerTone.Error   -> palette.errorBg   to palette.error
    }

/**
 * Returns the default Lucide icon for a given [InfoBannerTone].
 */
private fun defaultIcon(tone: InfoBannerTone): ImageVector = when (tone) {
    InfoBannerTone.Info    -> Lucide.Info
    InfoBannerTone.Success -> Lucide.CircleCheck
    InfoBannerTone.Warning -> Lucide.TriangleAlert
    InfoBannerTone.Error   -> Lucide.CircleX
}

/**
 * Informational banner atom.
 *
 * Card with 12.dp radius, 1.dp stroke, 14.dp padding, Row gap 12.dp:
 * - Leading icon 22.dp
 * - Column: [title] (13.sp Bold) + [description] (12.sp)
 *
 * Tone controls background, foreground, and stroke colors.
 *
 * @param title       Bold heading text.
 * @param description Secondary detail text.
 * @param modifier    Modifier applied to the outer container.
 * @param tone        Visual/semantic style (default [InfoBannerTone.Info]).
 * @param icon        Custom icon; defaults to the tone's canonical icon when null.
 */
@Composable
fun InfoBanner(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    tone: InfoBannerTone = InfoBannerTone.Info,
    icon: ImageVector? = null,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    val (bg, fg) = infoBannerColors(tone, colors)
    val resolvedIcon = icon ?: defaultIcon(tone)
    val shape = RoundedCornerShape(dimens.radiusMd)

    Row(
        verticalAlignment = Alignment.Top,
        modifier          = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg)
            .border(1.dp, fg, shape)
            .padding(14.dp),
    ) {
        Icon(
            imageVector        = resolvedIcon,
            contentDescription = null,
            tint               = fg,
            modifier           = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text       = title,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = fg,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text     = description,
                fontSize = 12.sp,
                color    = fg,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Composable
internal fun PreviewInfoBannerLight() {
    AppTheme(useDarkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoBannerTone.entries.forEach { tone ->
                InfoBanner(
                    title       = tone.name,
                    description = "Descripción para tono ${tone.name}",
                    tone        = tone,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun PreviewInfoBannerDark() {
    AppTheme(useDarkTheme = true) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoBannerTone.entries.forEach { tone ->
                InfoBanner(
                    title       = tone.name,
                    description = "Descripción para tono ${tone.name}",
                    tone        = tone,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
