package com.inclinic.app.ui.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inclinic.app.ui.theme.AppTheme

// ── Size enum ─────────────────────────────────────────────────────────────────

enum class AppButtonSize {
    Sm,  // 12.5sp, 12h / 6v padding
    Md,  // 13.5sp, 16h / 10v padding
    Lg,  // 14.5sp, 24h / 14v padding
}

// ── Variant enum ──────────────────────────────────────────────────────────────

enum class AppButtonVariant {
    Navy,    // filled navy → navyDark on press
    Outline, // transparent bg, navy border + text
    Ghost,   // no border, muted text
    Danger,  // filled red
}

// ── Public API ────────────────────────────────────────────────────────────────

/**
 * Branded button atom.
 *
 * @param text     Label shown in the button. Also shown when [loading] is true.
 * @param onClick  Click handler; no-op when [loading] or [enabled] is false.
 * @param variant  Visual style — [AppButtonVariant.Navy] by default.
 * @param size     Padding + font scale — [AppButtonSize.Md] by default.
 * @param loading  Show a small circular indicator alongside the text; disables interaction.
 * @param enabled  External disabled flag; 50 % alpha applied.
 * @param modifier Modifier for the button root — pass [Modifier.fillMaxWidth()] for full-width.
 */
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Navy,
    size: AppButtonSize = AppButtonSize.Md,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val colors   = AppTheme.colors
    val dimens   = AppTheme.dimens
    val isActive = enabled && !loading

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val shape        = RoundedCornerShape(dimens.radius)
    val contentPadding = size.contentPadding()
    val textStyle    = size.textStyle()

    val rootModifier = modifier.alpha(if (isActive) 1f else 0.5f)

    when (variant) {
        AppButtonVariant.Navy -> {
            val bgColor = if (isPressed && isActive) colors.navyDark else colors.navy
            Button(
                onClick           = { if (isActive) onClick() },
                enabled           = isActive,
                shape             = shape,
                colors            = ButtonDefaults.buttonColors(
                    containerColor         = bgColor,
                    contentColor           = Color.White,
                    disabledContainerColor = bgColor,
                    disabledContentColor   = Color.White,
                ),
                contentPadding    = contentPadding,
                interactionSource = interactionSource,
                modifier          = rootModifier,
            ) {
                ButtonContent(text = text, loading = loading, textStyle = textStyle, tint = Color.White)
            }
        }

        AppButtonVariant.Outline -> {
            OutlinedButton(
                onClick           = { if (isActive) onClick() },
                enabled           = isActive,
                shape             = shape,
                border            = BorderStroke(dimens.borderWidth, colors.navy),
                colors            = ButtonDefaults.outlinedButtonColors(
                    contentColor          = colors.navy,
                    disabledContentColor  = colors.navy,
                ),
                contentPadding    = contentPadding,
                interactionSource = interactionSource,
                modifier          = rootModifier,
            ) {
                ButtonContent(text = text, loading = loading, textStyle = textStyle, tint = colors.navy)
            }
        }

        AppButtonVariant.Ghost -> {
            TextButton(
                onClick           = { if (isActive) onClick() },
                enabled           = isActive,
                shape             = shape,
                colors            = ButtonDefaults.textButtonColors(
                    contentColor         = colors.muted,
                    disabledContentColor = colors.muted,
                ),
                contentPadding    = contentPadding,
                interactionSource = interactionSource,
                modifier          = rootModifier,
            ) {
                ButtonContent(text = text, loading = loading, textStyle = textStyle, tint = colors.muted)
            }
        }

        AppButtonVariant.Danger -> {
            Button(
                onClick           = { if (isActive) onClick() },
                enabled           = isActive,
                shape             = shape,
                colors            = ButtonDefaults.buttonColors(
                    containerColor         = colors.red,
                    contentColor           = Color.White,
                    disabledContainerColor = colors.red,
                    disabledContentColor   = Color.White,
                ),
                contentPadding    = contentPadding,
                interactionSource = interactionSource,
                modifier          = rootModifier,
            ) {
                ButtonContent(text = text, loading = loading, textStyle = textStyle, tint = Color.White)
            }
        }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    textStyle: TextStyle,
    tint: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (loading) {
            CircularProgressIndicator(
                color            = tint,
                strokeWidth      = 2.dp,
                modifier         = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = textStyle, color = tint)
    }
}

private fun AppButtonSize.contentPadding(): PaddingValues = when (this) {
    AppButtonSize.Sm -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    AppButtonSize.Md -> PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    AppButtonSize.Lg -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
}

private fun AppButtonSize.textStyle(): TextStyle = when (this) {
    AppButtonSize.Sm -> TextStyle(fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
    AppButtonSize.Md -> TextStyle(fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
    AppButtonSize.Lg -> TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
}
