package com.inclinic.app.features.auth.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.KeyRound
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.inclinic.app.features.auth.core.error.toUserMessage
import com.inclinic.app.features.auth.presentation.component.ForgotPasswordComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppIconCircle
import com.inclinic.app.ui.atoms.AppLink
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun ForgotPasswordScreen(
    component: ForgotPasswordComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors     = AppTheme.colors
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize()) {
            AuthScaffold {
                // ── Header ───────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier          = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 20.dp),
                ) {
                    AppBackButton(onClick = component::onBack)
                }

                // ── Illustration ─────────────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 32.dp, end = 32.dp, bottom = 8.dp),
                ) {
                    AppIconCircle(
                        icon        = Lucide.KeyRound,
                        bgColor     = colors.navyTint,
                        iconTint    = colors.navy,
                        circleSize  = 96.dp,
                        iconSize    = 44.dp,
                    )
                }

                // ── Copy ─────────────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                ) {
                    Text(
                        text      = "Recupera tu cuenta",
                        style     = typography.displaySmall,
                        color     = colors.text,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text      = "Ingresa tu correo y te enviaremos un enlace para restablecer tu contraseña",
                        style     = typography.body,
                        color     = colors.muted,
                        textAlign = TextAlign.Center,
                    )
                }

                // ── Form ─────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 24.dp, end = 24.dp),
                ) {
                    if (!state.isSent) {
                        val bannerMessage = state.error?.toUserMessage()
                        if (bannerMessage != null) {
                            ErrorBanner(message = bannerMessage, modifier = Modifier.fillMaxWidth())
                        }

                        AppTextField(
                            value         = state.email,
                            onValueChange = component::onEmailChanged,
                            label         = "Correo",
                            placeholder   = "tu@correo.com",
                            leadingIcon   = {
                                Icon(
                                    imageVector        = Lucide.Mail,
                                    contentDescription = null,
                                    tint               = colors.muted,
                                    modifier           = Modifier.size(18.dp),
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction    = ImeAction.Done,
                            ),
                            error   = state.emailError,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        Text(
                            text  = "Si el correo existe, recibirás un enlace para restablecer tu contraseña.",
                            style = typography.body,
                            color = colors.muted,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── CTA ──────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    if (!state.isSent) {
                        AppButton(
                            text     = "Enviar enlace",
                            onClick  = component::onSubmit,
                            loading  = state.isLoading,
                            enabled  = !state.isLoading,
                            size     = AppButtonSize.Lg,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector        = Lucide.ArrowLeft,
                            contentDescription = null,
                            tint               = colors.navy,
                            modifier           = Modifier.size(14.dp),
                        )
                        AppLink(
                            text       = " Volver al inicio de sesión",
                            onClick    = component::onBack,
                            emphasized = true,
                        )
                    }
                }

            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}
