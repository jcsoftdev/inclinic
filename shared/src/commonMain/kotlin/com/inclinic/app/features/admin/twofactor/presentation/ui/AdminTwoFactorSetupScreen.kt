package com.inclinic.app.features.admin.twofactor.presentation.ui

import androidx.compose.foundation.Image
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ScanLine
import com.inclinic.app.features.admin.twofactor.presentation.component.AdminTwoFactorSetupComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

/**
 * Admin 2FA setup screen.
 *
 * Design node: CaoqN
 *
 * QR rendering: uses qrose (io.github.alexzhirkevich:qrose:1.0.1), a KMP/Compose-native library.
 * [rememberQrCodePainter] encodes [TwoFactorSetup.provisioningUrl] (otpauth:// URI) into a
 * scannable QR image. If the URL is blank the area falls back to the manual-key placeholder.
 *
 * The manual [TwoFactorSetup.secret] is shown grouped in sets of 4 characters for readability.
 * Clipboard copy is shown as a hint; native clipboard is platform-specific and not wired here.
 * TODO: wire clipboard copy via expect/actual or a platform callback.
 */
@Composable
fun AdminTwoFactorSetupScreen(
    component: AdminTwoFactorSetupComponent,
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
                    text = "Configurar 2FA",
                    style = typography.displayXSmall,
                    color = colors.text,
                )
            }

            when {
                state.isLoadingSetup -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.navy)
                    }
                }

                state.setupError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimens.spacingMd),
                        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    ) {
                        ErrorBanner(message = state.setupError, modifier = Modifier.fillMaxWidth())
                    }
                }

                else -> {
                    val setup = state.setup
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(dimens.spacingMd),
                        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    ) {
                        InfoBanner(
                            title = "Usa tu app autenticadora",
                            description = "Google Authenticator, Authy o cualquier app TOTP compatible.",
                            tone = InfoBannerTone.Info,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // ── QR code ───────────────────────────────────────────
                        val provisioningUrl = setup?.provisioningUrl.orEmpty()
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(dimens.radiusMd))
                                .background(colors.surface)
                                .border(2.dp, colors.border, RoundedCornerShape(dimens.radiusMd)),
                        ) {
                            if (provisioningUrl.isNotBlank()) {
                                val qrPainter = rememberQrCodePainter(provisioningUrl)
                                Image(
                                    painter = qrPainter,
                                    contentDescription = "Código QR para autenticador",
                                    modifier = Modifier
                                        .size(168.dp)
                                        .padding(8.dp),
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(
                                        imageVector = Lucide.ScanLine,
                                        contentDescription = null,
                                        tint = colors.muted,
                                        modifier = Modifier.size(56.dp),
                                    )
                                    Text(
                                        text = "Usa la clave manual",
                                        color = colors.muted,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }

                        // ── Manual secret card ────────────────────────────────
                        if (setup != null) {
                            val formattedSecret = setup.secret
                                .chunked(4)
                                .joinToString(" ")

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(dimens.radiusMd))
                                    .background(colors.surface)
                                    .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusMd))
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "Clave manual",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.muted,
                                )
                                Text(
                                    text = formattedSecret,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text,
                                    letterSpacing = 1.5.sp,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Lucide.Copy,
                                        contentDescription = null,
                                        tint = colors.navy,
                                        modifier = Modifier.size(14.dp),
                                    )
                                    Text(
                                        text = "Copia la clave en tu app",
                                        fontSize = 12.sp,
                                        color = colors.navy,
                                    )
                                }
                                // Show provisioning URL as small selectable text (useful for manual entry)
                                Text(
                                    text = setup.provisioningUrl,
                                    fontSize = 9.sp,
                                    color = colors.light,
                                    lineHeight = 12.sp,
                                )
                            }
                        }

                        // ── Code input ────────────────────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (state.activateError != null) {
                                ErrorBanner(
                                    message = state.activateError,
                                    onDismiss = component::onErrorDismissed,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }

                            AppTextField(
                                value = state.code,
                                onValueChange = component::onCodeChange,
                                label = "Ingresa el código generado",
                                placeholder = "000000",
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                ),
                                enabled = !state.isActivating,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AppButton(
                            text = "Activar 2FA",
                            onClick = component::onActivate,
                            loading = state.isActivating,
                            enabled = state.canActivate,
                            size = AppButtonSize.Lg,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}
