package com.inclinic.app.features.auth.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import com.composables.icons.lucide.BadgeCheck
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.auth.core.error.toUserMessage
import com.inclinic.app.features.auth.presentation.component.ActivateComponent
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppIconCircle
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun ActivateScreen(
    component: ActivateComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        var showPassword by remember { mutableStateOf(false) }
        val colors     = AppTheme.colors
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize()) {
            AuthScaffold {
                // ── Illustration ─────────────────────────────────────────────
                Box(
                    contentAlignment = Alignment.Center,
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, start = 32.dp, end = 32.dp, bottom = 8.dp),
                ) {
                    AppIconCircle(
                        icon       = Lucide.BadgeCheck,
                        bgColor    = colors.greenBg,
                        iconTint   = colors.green,
                        circleSize = 108.dp,
                        iconSize   = 54.dp,
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
                        text      = "¡Cuenta aprobada!",
                        style     = typography.displayMedium,
                        color     = colors.text,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text      = "Define tu contraseña para activar tu cuenta de doctor en ClinicAI",
                        style     = typography.body,
                        color     = colors.muted,
                        textAlign = TextAlign.Center,
                    )
                }

                // ── Welcome card ─────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.navyTint)
                            .padding(14.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier         = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.navy),
                        ) {
                            Text(
                                text  = "DR",
                                style = typography.body.copy(
                                    color      = Color.White,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                ),
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier            = Modifier.weight(1f),
                        ) {
                            Text(
                                text  = "Bienvenido, Dr. ${component.email.substringBefore("@")}",
                                style = typography.body.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                ),
                                color = colors.text,
                            )
                            Text(
                                text  = "Cuenta verificada",
                                style = typography.body.copy(fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Normal),
                                color = colors.muted,
                            )
                        }
                    }
                }

                // ── Form ─────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 24.dp, end = 24.dp),
                ) {
                    val bannerMessage = state.error?.toUserMessage()
                    if (bannerMessage != null) {
                        ErrorBanner(message = bannerMessage, modifier = Modifier.fillMaxWidth())
                    }

                    AppTextField(
                        value         = state.code,
                        onValueChange = component::onCodeChanged,
                        label         = "Crear contraseña",
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
                        trailingIcon = {
                            Icon(
                                imageVector        = if (showPassword) Lucide.EyeOff else Lucide.Eye,
                                contentDescription = null,
                                tint               = colors.muted,
                                modifier           = Modifier.size(18.dp).clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication        = null,
                                    onClick           = { showPassword = !showPassword },
                                ),
                            )
                        },
                        enabled  = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Expiry notice — amber card
                    // Note: confirm-password field removed — the activate flow is token-only.
                    // POST /api/auth/activate accepts only the activation token (state.code).
                    // No password confirmation is required or possible at this stage.
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.amberBg)
                            .padding(12.dp),
                    ) {
                        Icon(
                            imageVector        = Lucide.Clock,
                            contentDescription = null,
                            tint               = colors.amber,
                            modifier           = Modifier.size(16.dp),
                        )
                        Text(
                            text  = "Este enlace expira en 7 días",
                            style = typography.caption,
                            color = colors.amber,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── CTA ──────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    AppButton(
                        text     = "Activar cuenta",
                        onClick  = component::onSubmit,
                        loading  = state.isLoading,
                        enabled  = !state.isLoading && state.code.isNotBlank(),
                        size     = AppButtonSize.Lg,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}
