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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MailCheck
import com.inclinic.app.features.auth.presentation.component.AccountCreatedComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppIconCircle
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

/**
 * "Cuenta Creada" confirmation screen — shown after a patient registers.
 *
 * Visual spec: 390px / $--sand bg.
 * Pattern mirrors [ActivateScreen]: AppTheme → AuthScaffold → sections stacked vertically.
 */
@Composable
fun AccountCreatedScreen(
    component: AccountCreatedComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state    by component.state.subscribeAsState()
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
                    icon       = Lucide.MailCheck,
                    bgColor    = colors.greenBg,
                    iconTint   = colors.green,
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
                    text      = "¡Cuenta creada!",
                    style     = typography.displayMedium,
                    color     = colors.text,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text      = "Te enviamos un correo a ${component.email} para activar tu cuenta. Revisa tu bandeja de entrada.",
                    style     = typography.body,
                    color     = colors.muted,
                    textAlign = TextAlign.Center,
                )
            }

            // ── Info banner ───────────────────────────────────────────────────
            AccountCreatedInfoBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Actions ───────────────────────────────────────────────────────
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                // Primary: go to login
                AppButton(
                    text     = "Ir a iniciar sesión",
                    onClick  = component::onGoToLogin,
                    variant  = AppButtonVariant.Navy,
                    size     = AppButtonSize.Lg,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Secondary: resend — label changes once sent
                AppButton(
                    text     = if (state.isResent) "Correo reenviado ✓" else "Reenviar correo",
                    onClick  = component::onResend,
                    variant  = AppButtonVariant.Outline,
                    size     = AppButtonSize.Lg,
                    enabled  = !state.isResent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

// ── Private composable ────────────────────────────────────────────────────────

/**
 * Info banner explaining what to do if the activation email doesn't arrive.
 * Built inline — no shared atom exists for this pattern yet.
 */
@Composable
private fun AccountCreatedInfoBanner(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
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
            text  = "¿No recibiste el correo? Revisa la carpeta de spam o reenvíalo abajo.",
            style = typography.body,
            color = colors.info,
        )
    }
}

// Bring typography into scope for the private composable without passing it as a param.
private val typography @Composable get() = AppTheme.typography
