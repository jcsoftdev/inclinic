package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.CloudOff
import com.composables.icons.lucide.Lucide
import com.inclinic.app.ui.theme.AppTheme

/**
 * Full-body centered error state -- matches the Doctor-Estado-Error design (XXHht).
 *
 * Renders a cloud-off icon in a bordered circle, a title, a subtitle,
 * and an optional retry CTA button. Sized to fill its parent so it
 * occupies the whole content area when placed inside a Column/Box.
 *
 * @param title       Primary error heading (default: "No se pudo cargar").
 * @param subtitle    Descriptive subtext (default: "Revisa tu conexion e intentalo de nuevo.").
 * @param retryLabel  Label for the retry CTA. Shown only when non-null.
 * @param onRetry     Callback for the retry button.
 * @param modifier    Modifier applied to the root Column.
 */
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    title: String = "No se pudo cargar",
    subtitle: String = "Revisa tu conexion e intentalo de nuevo.",
    retryLabel: String? = "Reintentar",
    onRetry: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 24.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.sand),
        ) {
            Icon(
                imageVector        = Lucide.CloudOff,
                contentDescription = null,
                tint               = colors.muted,
                modifier           = Modifier.size(34.dp),
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text       = title,
            fontSize   = 21.sp,
            fontWeight = FontWeight.Bold,
            color      = colors.text,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text      = subtitle,
            fontSize  = 14.sp,
            fontWeight = FontWeight.Medium,
            color     = colors.muted,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth(),
        )

        if (retryLabel != null && onRetry != null) {
            Spacer(Modifier.height(16.dp))
            AppButton(
                text    = retryLabel,
                onClick = onRetry,
                variant = AppButtonVariant.Navy,
                size    = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
