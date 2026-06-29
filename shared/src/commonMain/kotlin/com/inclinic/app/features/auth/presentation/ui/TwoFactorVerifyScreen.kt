package com.inclinic.app.features.auth.presentation.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.templates.AuthScaffold
import com.inclinic.app.ui.theme.AppTheme

/**
 * 2FA verification screen — step 2 of login.
 *
 * Design node: pJKrT
 * Shows a 6-digit TOTP code as individual input boxes with focus auto-advance.
 * Includes an InfoBanner for SUPER_ADMIN accounts and circular icon background.
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

                // ── Circular icon + title ─────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 32.dp, end = 32.dp),
                ) {
                    // Circular icon background per design
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .background(colors.navyTint, CircleShape),
                    ) {
                        Icon(
                            imageVector = Lucide.ShieldCheck,
                            contentDescription = null,
                            tint = colors.navy,
                            modifier = Modifier.size(36.dp),
                        )
                    }

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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
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

                    // 6-box OTP input
                    OtpInputRow(
                        code = state.code,
                        enabled = !state.isSubmitting,
                        onCodeChange = component::onCodeChange,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "El código cambia cada 30 segundos. Ábrelo en tu app autenticadora.",
                        fontSize = 12.sp,
                        color = colors.muted,
                    )

                    // InfoBanner for SUPER_ADMIN requirement
                    InfoBanner(
                        title = "Verificación obligatoria",
                        description = "La verificación 2FA es obligatoria para cuentas SUPER_ADMIN.",
                        tone = InfoBannerTone.Info,
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

/**
 * 6-box OTP input row.
 *
 * Renders one [BasicTextField] per digit. The entire code is managed as a
 * single String — the focused box index is `code.length` (the next empty slot).
 * Focus auto-advances as each digit is entered and steps back on delete.
 */
@Composable
private fun OtpInputRow(
    code: String,
    enabled: Boolean,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Drive focus to the next empty box whenever code changes.
    LaunchedEffect(code) {
        val target = code.length.coerceIn(0, 5)
        focusRequesters[target].requestFocus()
    }

    // A single invisible TextField that holds the full 6-char value; we render
    // the boxes ourselves and intercept changes to keep code <= 6 digits.
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        repeat(6) { index ->
            val digit = code.getOrNull(index)?.toString() ?: ""
            val isFocused = index == code.length.coerceAtMost(5)

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .background(
                        color = if (isFocused) colors.navyTint else colors.base,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .border(
                        width = if (isFocused) 2.dp else 1.dp,
                        color = if (isFocused) colors.navy else colors.border,
                        shape = RoundedCornerShape(10.dp),
                    ),
            ) {
                BasicTextField(
                    value = digit,
                    onValueChange = { input ->
                        if (!enabled) return@BasicTextField
                        val filtered = input.filter { it.isDigit() }
                        when {
                            // Digit entered — append to code
                            filtered.isNotEmpty() && code.length < 6 -> {
                                onCodeChange(code + filtered.last())
                            }
                            // Backspace / delete — remove last char
                            filtered.isEmpty() && digit.isNotEmpty() -> {
                                onCodeChange(code.dropLast(1))
                            }
                        }
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 22.sp,
                        color = colors.text,
                        textAlign = TextAlign.Center,
                    ),
                    cursorBrush = SolidColor(colors.navy),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = if (index == 5) ImeAction.Done else ImeAction.Next,
                    ),
                    enabled = enabled,
                    modifier = Modifier
                        .width(24.dp)
                        .focusRequester(focusRequesters[index]),
                    decorationBox = { inner ->
                        if (digit.isEmpty()) {
                            Text(
                                text = "·",
                                fontSize = 22.sp,
                                color = colors.light,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        } else {
                            inner()
                        }
                    },
                )
            }
        }
    }
}
