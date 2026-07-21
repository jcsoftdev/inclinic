package com.inclinic.app.features.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.auth.presentation.component.PatientRateLimitComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppIconCircle
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

/**
 * Standalone screen shown when auth requests (login, currently) return HTTP 429 —
 * too many attempts in a short window ([com.inclinic.app.features.auth.core.error.AuthError.TooManyAttempts]).
 *
 * Previously this error only produced an inline banner via `AuthErrorMessages.toUserMessage()`.
 * It now routes here instead, matching the visual pattern of [AccountCreatedScreen] /
 * [RegisterChooserScreen]: `AppTheme` → `AuthScaffold` → icon circle → copy → info banner → CTA.
 */
@Composable
fun PatientRateLimitScreen(
    component: PatientRateLimitComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val colors     = AppTheme.colors
        val typography = AppTheme.typography

        AuthScaffold(modifier = modifier) {

            // ── Illustration ──────────────────────────────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 32.dp, end = 32.dp, bottom = 8.dp),
            ) {
                AppIconCircle(
                    icon       = Lucide.Clock,
                    bgColor    = colors.amberBg,
                    iconTint   = colors.amber,
                    circleSize = 108.dp,
                    iconSize   = 54.dp,
                )
            }

            // ── Copy ─────────────────────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
            ) {
                Text(
                    text      = "Demasiados intentos",
                    style     = typography.displayMedium,
                    color     = colors.text,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text      = "Detectamos varios intentos de inicio de sesión seguidos. " +
                        "Por tu seguridad, espera unos minutos antes de volver a intentarlo.",
                    style     = typography.body,
                    color     = colors.muted,
                    textAlign = TextAlign.Center,
                )
            }

            // ── Info banner ───────────────────────────────────────────────────
            RateLimitInfoBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.weight(1f))

            // ── Actions ───────────────────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                AppButton(
                    text     = "Volver a iniciar sesión",
                    onClick  = component::onBackToLogin,
                    variant  = AppButtonVariant.Navy,
                    size     = AppButtonSize.Lg,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── Private composable ────────────────────────────────────────────────────────

@Composable
private fun RateLimitInfoBanner(modifier: Modifier = Modifier) {
    val colors     = AppTheme.colors
    val typography = AppTheme.typography
    Row(
        verticalAlignment     = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier              = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.infoBg)
            .border(1.dp, colors.infoBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector        = Lucide.Info,
            contentDescription = null,
            tint               = colors.info,
            modifier           = Modifier.size(18.dp),
        )
        Text(
            text  = "Si el problema persiste, verifica tu correo y contraseña o contacta a soporte.",
            style = typography.body,
            color = colors.info,
        )
    }
}
