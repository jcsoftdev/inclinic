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
import com.composables.icons.lucide.Eye
import com.composables.icons.lucide.EyeOff
import com.composables.icons.lucide.Lock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.Phone
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
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.auth.core.error.toUserMessage
import com.inclinic.app.features.auth.presentation.component.RegisterPatientComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppLink
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.atoms.PasswordStrengthBar
import com.inclinic.app.ui.atoms.PasswordStrength
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RegisterPatientScreen(
    component: RegisterPatientComponent,
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

                // ── Hero ─────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
                ) {
                    Text(
                        text  = "Crear cuenta",
                        style = typography.displayMedium,
                        color = colors.text,
                    )
                    Text(
                        text  = "Empieza a cuidar tu salud en minutos",
                        style = typography.subtitle,
                        color = colors.muted,
                    )
                }

                // ── Form ─────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    val bannerMessage = state.serverError?.toUserMessage()
                    if (bannerMessage != null) {
                        ErrorBanner(message = bannerMessage, modifier = Modifier.fillMaxWidth())
                    }

                    // First + Last name row
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppTextField(
                            value         = state.name,
                            onValueChange = component::onNameChanged,
                            label         = "Nombre",
                            placeholder   = "Juan",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            error         = state.nameError,
                            enabled       = !state.isLoading,
                            modifier      = Modifier.weight(1f),
                        )
                        AppTextField(
                            value         = state.lastName,
                            onValueChange = component::onLastNameChanged,
                            label         = "Apellido",
                            placeholder   = "Pérez",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            error         = state.lastNameError,
                            enabled       = !state.isLoading,
                            modifier      = Modifier.weight(1f),
                        )
                    }

                    AppTextField(
                        value         = state.email,
                        onValueChange = component::onEmailChanged,
                        label         = "Correo electrónico",
                        placeholder   = "juan@correo.com",
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
                        error   = state.emailError,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    AppTextField(
                        value         = state.phone,
                        onValueChange = component::onPhoneChanged,
                        label         = "Teléfono (opcional)",
                        placeholder   = "+51 999 888 777",
                        leadingIcon   = {
                            Icon(
                                imageVector        = Lucide.Phone,
                                contentDescription = null,
                                tint               = colors.muted,
                                modifier           = Modifier.size(18.dp),
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction    = ImeAction.Next,
                        ),
                        enabled  = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    AppTextField(
                        value         = state.password,
                        onValueChange = component::onPasswordChanged,
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
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None
                                               else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction    = ImeAction.Done,
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector        = if (showConfirmPassword) Lucide.EyeOff else Lucide.Eye,
                                contentDescription = null,
                                tint               = colors.muted,
                                modifier           = Modifier.size(18.dp).clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication        = null,
                                    onClick           = { showConfirmPassword = !showConfirmPassword },
                                ),
                            )
                        },
                        error   = state.confirmPasswordError,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── CTA ──────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    AppButton(
                        text     = "Crear Cuenta",
                        onClick  = component::onSubmit,
                        loading  = state.isLoading,
                        enabled  = !state.isLoading,
                        size     = AppButtonSize.Lg,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically,
                        modifier              = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text  = "¿Ya tienes cuenta? ",
                            style = typography.subtitle,
                            color = colors.muted,
                        )
                        AppLink(text = "Inicia sesión", onClick = component::onBack)
                    }
                }

            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}
