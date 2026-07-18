package com.inclinic.app.ui.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Lucide
import com.inclinic.app.ui.theme.AppTheme

/**
 * Shared full-screen error / not-found state for "load one entity by id" screens
 * (admin appointment/doctor/pending-doctor detail, resolve-dispute, resolve-no-show).
 *
 * Replaces the raw red [Text] with no affordance that those 5 screens used to
 * render on load failure — always offers a way back to the list via [onBackToList].
 * Pair with [com.inclinic.app.core.util.DetailLoadState] to decide when to show it.
 *
 * @param message     User-facing error text (already localized by the component).
 * @param onBackToList Callback for the back-to-list CTA — typically `component::onBack`.
 * @param notFound    When true, renders "No encontrado" copy/tone instead of the
 *                     generic "No se pudo cargar" — pass `DetailLoadState.NotFound`.
 * @param backLabel   CTA label, overridable per screen if "Volver a la lista" doesn't fit.
 */
@Composable
fun DetailErrorState(
    message: String,
    onBackToList: () -> Unit,
    modifier: Modifier = Modifier,
    notFound: Boolean = false,
    backLabel: String = "Volver a la lista",
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val title = if (notFound) "No encontrado" else "No se pudo cargar"

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(dimens.spacingLg),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(colors.sand),
            ) {
                Icon(
                    imageVector = Lucide.CircleAlert,
                    contentDescription = null,
                    tint = colors.red,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.height(dimens.spacingSm))

            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = message,
                fontSize = 13.sp,
                color = colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(dimens.spacingMd))

            AppButton(
                text = backLabel,
                onClick = onBackToList,
                variant = AppButtonVariant.Outline,
                size = AppButtonSize.Md,
            )
        }
    }
}
