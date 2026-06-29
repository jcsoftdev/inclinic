package com.inclinic.app.features.admin.twofactor.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldOff
import com.inclinic.app.features.admin.twofactor.presentation.component.AdminSecurityComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme

/**
 * Admin security screen — 2FA status and management.
 *
 * Design node: w3WcCL
 * Shows 2FA status, enforced warning when applicable, CTA to setup/disable.
 */
@Composable
fun AdminSecurityScreen(
    component: AdminSecurityComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val typography = AppTheme.typography
        val dimens = AppTheme.dimens

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(colors.sand),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                AppBackButton(onClick = component::onBack)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Seguridad",
                    style = typography.displayXSmall,
                    color = colors.text,
                )
            }

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.navy)
                    }
                }

                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimens.spacingMd),
                        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    ) {
                        ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())
                        AppButton(
                            text = "Reintentar",
                            onClick = component::onRetry,
                            variant = AppButtonVariant.Outline,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                else -> {
                    val status = state.status
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(dimens.spacingMd),
                        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    ) {
                        // Success message (after disable)
                        if (state.successMessage != null) {
                            InfoBanner(
                                title = "Listo",
                                description = state.successMessage!!,
                                tone = InfoBannerTone.Success,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        if (status != null && status.enforced) {
                            InfoBanner(
                                title = "2FA obligatorio",
                                description = "Tu organización exige verificación en dos pasos para administradores",
                                tone = InfoBannerTone.Warning,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        if (status != null && status.enabled) {
                            // ── 2FA enabled state ─────────────────────────────────────────
                            InfoBanner(
                                title = "2FA activado",
                                description = buildString {
                                    append("App TOTP")
                                    if (status.verifiedAt != null) {
                                        append(" · Verificado el ")
                                        append(status.verifiedAt.take(10))
                                    }
                                },
                                tone = InfoBannerTone.Success,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Status rows
                            SecurityStatusCard {
                                SecurityRow(label = "Estado", value = "Activo", colors.success)
                                SecurityRow(label = "Método", value = "App TOTP", colors.text)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            AppButton(
                                text = "Desactivar 2FA",
                                onClick = component::onDisableTwoFactor,
                                variant = AppButtonVariant.Danger,
                                size = AppButtonSize.Md,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            // ── 2FA not enabled state ─────────────────────────────────────
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                            ) {
                                Icon(
                                    imageVector = Lucide.ShieldOff,
                                    contentDescription = null,
                                    tint = colors.muted,
                                    modifier = Modifier.size(56.dp),
                                )
                                Text(
                                    text = "2FA no activado",
                                    style = typography.displayXSmall,
                                    color = colors.text,
                                )
                                Text(
                                    text = "Activa la verificación en dos pasos para proteger tu cuenta de administrador.",
                                    color = colors.muted,
                                    fontSize = 13.sp,
                                )
                            }

                            AppButton(
                                text = "Configurar 2FA",
                                onClick = component::onSetupTwoFactor,
                                variant = AppButtonVariant.Navy,
                                size = AppButtonSize.Lg,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }

        // ── Disable dialog ────────────────────────────────────────────────────
        if (state.showDisableDialog) {
            DisableTwoFactorDialog(
                error = state.disableError,
                isLoading = state.isDisabling,
                onConfirm = { code -> component.onDisableConfirm(code) },
                onDismiss = component::onDisableDialogDismiss,
            )
        }
    }
}

@Composable
private fun SecurityStatusCard(content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppTheme.dimens.radiusMd)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun SecurityRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = colors.muted, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DisableTwoFactorDialog(
    error: String?,
    isLoading: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val code = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Desactivar 2FA") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ingresa tu código actual de 6 dígitos para confirmar.",
                    fontSize = 13.sp,
                )
                if (error != null) {
                    ErrorBanner(message = error, modifier = Modifier.fillMaxWidth())
                }
                AppTextField(
                    value = code.value,
                    onValueChange = { input ->
                        code.value = input.filter { it.isDigit() }.take(6)
                    },
                    label = "Código TOTP",
                    placeholder = "000000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(code.value) },
                enabled = code.value.length == 6 && !isLoading,
            ) {
                Text("Desactivar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
