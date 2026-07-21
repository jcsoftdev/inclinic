package com.inclinic.app.features.auth.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.HeartPulse
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.testTag
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.auth.core.error.toUserMessage
import com.inclinic.app.features.auth.presentation.component.LoginComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppLink
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun LoginScreen(
    component: LoginComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        var showPassword by remember { mutableStateOf(false) }
        val colors     = AppTheme.colors
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize()) {
            AuthScaffold {
                // ── Brand ────────────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 32.dp, end = 32.dp, bottom = 32.dp),
                ) {
                    // Navy circle with heart-pulse icon
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier         = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(colors.navy),
                    ) {
                        Icon(
                            imageVector        = Lucide.HeartPulse,
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(40.dp),
                        )
                    }

                    Text(
                        text      = "ClinicAI",
                        style     = typography.displayLarge,
                        color     = colors.text,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text      = "Tu salud, en buenas manos",
                        style     = typography.body,
                        color     = colors.muted,
                        textAlign = TextAlign.Center,
                    )
                }

                // ── Form ─────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    // A real 401/token-expiry (never an explicit logout) surfaces once here.
                    val bannerMessage = state.authError?.toUserMessage() ?: state.sessionExpiredMessage
                    if (bannerMessage != null) {
                        ErrorBanner(message = bannerMessage, modifier = Modifier.fillMaxWidth())
                    }

                    // Inactive/unverified account — offer to resend the activation email
                    // instead of leaving the user stuck (design-gap-closure).
                    if (state.canResendActivation) {
                        AppLink(
                            text = if (state.resendActivationSent) {
                                "Correo de activación reenviado"
                            } else {
                                "Reenviar correo de activación"
                            },
                            onClick = component::onResendActivation,
                            emphasized = true,
                        )
                    }

                    // Suspended account — point the user to support instead of a dead end.
                    if (state.isSuspended) {
                        Text(
                            text = "Si crees que esto es un error, contacta a soporte.",
                            style = typography.subtitle,
                            color = colors.muted,
                        )
                    }

                    AppTextField(
                        value         = state.email,
                        onValueChange = component::onEmailChange,
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
                            imeAction    = ImeAction.Next,
                        ),
                        error        = state.emailError,
                        enabled      = !state.isSubmitting,
                        modifier     = Modifier.fillMaxWidth(),
                        inputTestTag = "login_email_field",
                    )

                    AppTextField(
                        value         = state.password,
                        onValueChange = component::onPasswordChange,
                        label         = "Contraseña",
                        placeholder   = "••••••••",
                        leadingIcon   = {
                            Icon(
                                imageVector        = Lucide.Lock,
                                contentDescription = null,
                                tint               = colors.muted,
                                modifier           = Modifier.size(18.dp),
                            )
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { component.onSubmit() }),
                        error        = state.passwordError,
                        enabled      = !state.isSubmitting,
                        trailingIcon = {
                            Icon(
                                imageVector        = if (showPassword) Lucide.EyeOff else Lucide.Eye,
                                contentDescription = null,
                                tint               = colors.muted,
                                modifier           = Modifier
                                    .size(18.dp)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication        = null,
                                        onClick           = { showPassword = !showPassword },
                                    ),
                            )
                        },
                        modifier     = Modifier.fillMaxWidth(),
                        inputTestTag = "login_password_field",
                    )

                    // Forgot password — right-aligned
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        AppLink(
                            text       = "¿Olvidaste tu contraseña?",
                            onClick    = component::onForgotPassword,
                            emphasized = true,
                        )
                    }
                }

                // ── Spacer pushes CTA to bottom ───────────────────────────
                Spacer(modifier = Modifier.weight(1f))

                // ── CTA ──────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    AppButton(
                        text     = "Iniciar Sesión",
                        onClick  = component::onSubmit,
                        loading  = state.isSubmitting,
                        enabled  = !state.isSubmitting,
                        size     = AppButtonSize.Lg,
                        modifier = Modifier.fillMaxWidth().testTag("login_button"),
                    )

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text  = "¿No tienes cuenta? ",
                            style = typography.subtitle,
                            color = colors.muted,
                        )
                        AppLink(
                            text       = "Regístrate",
                            onClick    = component::onRegister,
                            emphasized = true,
                        )
                    }
                }

            }

            LoadingOverlay(visible = state.isSubmitting)
        }
    }
}
