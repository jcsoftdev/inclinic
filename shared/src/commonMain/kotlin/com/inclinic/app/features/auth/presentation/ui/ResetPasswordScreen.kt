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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Icon
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.auth.core.error.toUserMessage
import com.inclinic.app.features.auth.presentation.component.ResetPasswordComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppIconCircle
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.atoms.PasswordStrength
import com.inclinic.app.ui.atoms.PasswordStrengthBar
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun ResetPasswordScreen(
    component: ResetPasswordComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        var showPassword by remember { mutableStateOf(false) }
        var showConfirmPassword by remember { mutableStateOf(false) }
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
                        icon       = Lucide.ShieldCheck,
                        bgColor    = colors.tealBg,
                        iconTint   = colors.teal,
                        circleSize = 96.dp,
                        iconSize   = 44.dp,
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
                        text      = "Define nueva contraseña",
                        style     = typography.displayXSmall,
                        color     = colors.text,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text      = "Usa al menos 8 caracteres, una mayúscula y un número",
                        style     = typography.subtitle,
                        color     = colors.muted,
                        textAlign = TextAlign.Center,
                    )
                }

                // ── Form ─────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 24.dp, end = 24.dp),
                ) {
                    val bannerMessage = state.error?.toUserMessage()
                    if (bannerMessage != null) {
                        ErrorBanner(message = bannerMessage, modifier = Modifier.fillMaxWidth())
                    }

                    AppTextField(
                        value         = state.password,
                        onValueChange = component::onPasswordChanged,
                        label         = "Nueva contraseña",
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
                            imeAction    = ImeAction.Next,
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
                        error   = state.passwordError,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    if (state.password.isNotEmpty()) {
                        val strength = when {
                            state.password.length >= 12 -> PasswordStrength.Strong
                            state.password.length >= 8  -> PasswordStrength.Good
                            state.password.length >= 5  -> PasswordStrength.Fair
                            else                         -> PasswordStrength.Weak
                        }
                        PasswordStrengthBar(strength = strength)

                        if (strength >= PasswordStrength.Good) {
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector        = Lucide.ShieldCheck,
                                    contentDescription = null,
                                    tint               = colors.green,
                                    modifier           = Modifier.size(13.dp),
                                )
                                Text(
                                    text  = "Fuerza: ${if (strength == PasswordStrength.Strong) "alta" else "buena"}",
                                    style = typography.caption,
                                    color = colors.green,
                                )
                            }
                        }
                    }

                    AppTextField(
                        value         = state.confirmPassword,
                        onValueChange = component::onConfirmPasswordChanged,
                        label         = "Confirmar contraseña",
                        placeholder   = "••••••••",
                        leadingIcon   = {
                            Icon(
                                imageVector        = Lucide.Lock,
                                contentDescription = null,
                                tint               = colors.muted,
                                modifier           = Modifier.size(18.dp),
                            )
                        },
                        trailingIcon = if (state.confirmPassword.isNotEmpty() && state.confirmPassword == state.password) {
                            {
                                Icon(
                                    imageVector        = Lucide.Check,
                                    contentDescription = null,
                                    tint               = colors.green,
                                    modifier           = Modifier.size(18.dp),
                                )
                            }
                        } else null,
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done,
                        ),
                        error   = state.confirmPasswordError,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── CTA ──────────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    AppButton(
                        text     = "Guardar contraseña",
                        onClick  = component::onSubmit,
                        loading  = state.isLoading,
                        enabled  = !state.isLoading,
                        size     = AppButtonSize.Lg,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}
