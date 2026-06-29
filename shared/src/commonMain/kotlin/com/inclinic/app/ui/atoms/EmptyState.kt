package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Inbox
import com.composables.icons.lucide.Lucide
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.theme.AppTheme

/**
 * Empty state atom.
 *
 * Centered vertical column with a 56.dp circle icon wrap, a [title] (15.sp SemiBold, muted)
 * and a [subtitle] (13.sp, light, max 260.dp width, centered).
 *
 * @param title       Primary empty-state heading.
 * @param subtitle    Descriptive subtext.
 * @param modifier    Modifier applied to the root [Column].
 * @param icon        Icon to show (default: [Lucide.Inbox]).
 * @param actionLabel Optional CTA button label. Shown when non-null.
 * @param onAction    Callback for the CTA button. Called when [actionLabel] is non-null and button is tapped.
 */
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Lucide.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = modifier.padding(24.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier         = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.sand),
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                tint               = colors.light,
                modifier           = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text      = title,
            fontSize  = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color     = colors.muted,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text      = subtitle,
            fontSize  = 13.sp,
            color     = colors.light,
            textAlign = TextAlign.Center,
            modifier  = Modifier.width(260.dp),
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(16.dp))
            AppButton(
                text    = actionLabel,
                onClick = onAction,
                variant = AppButtonVariant.Navy,
                size    = AppButtonSize.Md,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Composable
internal fun PreviewEmptyStateLight() {
    AppTheme(useDarkTheme = false) {
        EmptyState(
            title    = "Sin citas",
            subtitle = "No tienes citas programadas por ahora",
        )
    }
}

@Composable
internal fun PreviewEmptyStateDark() {
    AppTheme(useDarkTheme = true) {
        EmptyState(
            title    = "Sin citas",
            subtitle = "No tienes citas programadas por ahora",
        )
    }
}
