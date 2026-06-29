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
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import com.inclinic.app.features.auth.core.error.toUserMessage
import com.inclinic.app.features.auth.presentation.component.TwoFactorVerifyComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

/**
 * 2FA verification screen — step 2 of login.
 *
 * Design node: pJKrT
 * Shows a 6-digit TOTP code input and verifies against the backend.
 */
@Composable
fun TwoFactorVerifyScreen(
    component: TwoFactorVerifyComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize()) {
            AuthScaffold {
                // ── Header ────────────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 12.dp, end = 12.dp),
                ) {
                    AppBackButton(onClick = component::onBack)
                }

                // ── Icon + title ──────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 32.dp, end = 32.dp),
                ) {
                    Icon(
                        imageVector = Lucide.ShieldCheck,
                        contentDescription = null,
                        tint = colors.navy,
                        modifier = Modifier.size(56.dp),
                    )

                    Text(
                        text = "Verificación 2FA",
                        style = typography.displayXSmall,
                        color = colors.text,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "Ingresa tu código de 6 dígitos",
                        style = typography.body,
                        color = colors.muted,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Form ─────────────────────────────────────────────────────
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                ) {
                    val bannerMessage = state.authError?.toUserMessage()
                    if (bannerMessage != null) {
                        ErrorBanner(
                            message = bannerMessage,
                            onDismiss = component::onErrorDismissed,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    AppTextField(
                        value = state.code,
                        onValueChange = component::onCodeChange,
                        label = "Código TOTP",
                        placeholder = "000000",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done,
                        ),
                        enabled = !state.isSubmitting,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "El código cambia cada 30 segundos. Ábrelo en tu app autenticadora.",
                        fontSize = 12.sp,
                        color = colors.muted,
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
                        text = "Verificar",
                        onClick = component::onVerify,
                        loading = state.isSubmitting,
                        enabled = state.canVerify,
                        size = AppButtonSize.Lg,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            LoadingOverlay(visible = state.isSubmitting)
        }
    }
}
