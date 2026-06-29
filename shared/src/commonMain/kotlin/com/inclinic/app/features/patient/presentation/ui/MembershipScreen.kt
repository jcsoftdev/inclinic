package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Star
import com.inclinic.app.core.model.SubscriptionTier
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.patient.presentation.component.CardType
import com.inclinic.app.features.patient.presentation.component.MembershipComponent
import com.inclinic.app.features.patient.presentation.component.MembershipState
import com.inclinic.app.features.patient.presentation.component.PREMIUM_PRICE
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    component: MembershipComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                // Parent PatientFlowContent already consumes the status-bar inset; without
                // this the bar would pad it a second time and sit too low.
                windowInsets = WindowInsets(0),
                title = { Text("Membresía", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Atrás", tint = colors.text)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.sand,
                    titleContentColor = colors.text,
                ),
            )
        },
        bottomBar = {
            if (!state.isLoading) {
                val ctaText = if (state.isPremium) {
                    "Renovar (+30 días) · S/ ${PREMIUM_PRICE.formatDecimal(2)}"
                } else {
                    "Mejorar a Premium · S/ ${PREMIUM_PRICE.formatDecimal(2)}/mes"
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.sand)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    AppButton(
                        text = ctaText,
                        onClick = component::onUpgradeTapped,
                        variant = AppButtonVariant.Navy,
                        size = AppButtonSize.Lg,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        containerColor = colors.sand,
        modifier = modifier.fillMaxSize(),
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.navy, strokeWidth = 3.dp)
            }
            return@Scaffold
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 8.dp),
        ) {
            HeroCard(state = state)

            // Top-level error (e.g. load failure) shown outside the checkout sheet.
            if (!state.showCheckout) {
                state.error?.let { ErrorBanner(message = it, onDismiss = component::onErrorDismissed) }
            }

            Text("Beneficios", color = colors.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            // Weighted + SpaceBetween so the benefit cards distribute evenly down to the
            // pinned CTA instead of bunching at the top with a void below.
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                modifier = Modifier.fillMaxWidth().weight(1f),
            ) {
                state.benefits.forEach { benefit ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.elevated)
                            .padding(16.dp),
                    ) {
                        Icon(
                            Lucide.Check,
                            contentDescription = null,
                            tint = colors.navy,
                            modifier = Modifier.padding(top = 1.dp).size(18.dp),
                        )
                        Text(benefit, color = colors.text, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        if (state.showCheckout) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = component::onDismissCheckout,
                sheetState = sheetState,
                containerColor = colors.sand,
            ) {
                CheckoutForm(component = component, state = state)
            }
        }
    }
}

// ── Hero ────────────────────────────────────────────────────────────────────

@Composable
private fun HeroCard(state: MembershipState) {
    val colors = AppTheme.colors
    val premium = state.tier == SubscriptionTier.PREMIUM

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (premium) colors.navy else colors.elevated)
            .padding(24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(
                Lucide.Star,
                contentDescription = null,
                tint = if (premium) Color.White else colors.muted,
                modifier = Modifier.size(26.dp),
            )
            Text(
                text = if (premium) "Plan Premium" else "Plan Gratuito",
                color = if (premium) Color.White else colors.text,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Status chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(if (premium) Color.White.copy(alpha = 0.16f) else colors.navyTint)
                .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
            Text(
                text = if (premium) statusLabel(state.expiresAt) else "Mejora para más beneficios",
                color = if (premium) Color.White else colors.navy,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (!premium) {
            Text(
                text = "Desbloquea tu historia clínica completa y mucho más por solo S/ ${PREMIUM_PRICE.formatDecimal(2)} al mes.",
                color = colors.muted,
                fontSize = 13.sp,
            )
        }
    }
}

private fun statusLabel(expiresAt: String?): String {
    val date = expiresAt?.take(10) // ISO date portion (YYYY-MM-DD)
    return if (date != null) "Activo hasta $date" else "Activo"
}

// ── Checkout (card form) ──────────────────────────────────────────────────────

@Composable
private fun CheckoutForm(
    component: MembershipComponent,
    state: MembershipState,
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "Pagar membresía Premium",
            color = colors.text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        // Price summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.elevated)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Premium · 30 días", color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("Cobro único · sin auto-renovación", color = colors.muted, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Text(
                text = "S/ ${PREMIUM_PRICE.formatDecimal(2)}",
                color = colors.navy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
            )
        }

        state.error?.let { ErrorBanner(message = it, onDismiss = component::onErrorDismissed) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.elevated)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CheckoutInput(
                label = "Número de tarjeta",
                value = state.cardNumber,
                placeholder = "4242 4242 4242 4242",
                keyboardType = KeyboardType.Number,
                leading = {
                    Icon(Lucide.CreditCard, contentDescription = null, tint = colors.muted, modifier = Modifier.size(18.dp))
                },
                trailing = cardBrandLabel(state.cardType),
                onValueChange = component::onCardNumberChange,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CheckoutInput(
                    label = "Vencimiento",
                    value = state.expiry,
                    placeholder = "MM/YY",
                    keyboardType = KeyboardType.Number,
                    onValueChange = component::onExpiryChange,
                    modifier = Modifier.weight(1f),
                )
                CheckoutInput(
                    label = "CVV",
                    value = state.cvv,
                    placeholder = "123",
                    keyboardType = KeyboardType.Number,
                    onValueChange = component::onCvvChange,
                    modifier = Modifier.weight(1f),
                )
            }

            CheckoutInput(
                label = "Titular",
                value = state.cardholderName,
                placeholder = "Juan Pérez",
                onValueChange = component::onCardholderNameChange,
            )

            CheckoutInput(
                label = "DNI / documento",
                value = state.docNumber,
                placeholder = "12345678",
                keyboardType = KeyboardType.Number,
                onValueChange = component::onDocNumberChange,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(Lucide.ShieldCheck, contentDescription = null, tint = colors.green, modifier = Modifier.size(14.dp))
            Text(
                text = "Pago seguro con MercadoPago",
                color = colors.green,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (state.isPurchasing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.navy),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            }
        } else {
            AppButton(
                text = "Pagar S/ ${PREMIUM_PRICE.formatDecimal(2)}",
                onClick = component::onSubmitPurchase,
                variant = AppButtonVariant.Navy,
                size = AppButtonSize.Lg,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun CheckoutInput(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    leading: (@Composable () -> Unit)? = null,
    trailing: String = "",
) {
    val colors = AppTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            color = colors.muted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.4.sp,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.sand)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            leading?.invoke()
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(color = colors.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                cursorBrush = SolidColor(colors.navy),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isBlank()) Text(placeholder, color = colors.light, fontSize = 14.sp)
                        inner()
                    }
                },
            )
            if (trailing.isNotBlank()) {
                Text(trailing, color = colors.navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun cardBrandLabel(cardType: CardType): String = when (cardType) {
    CardType.VISA -> "VISA"
    CardType.MASTERCARD -> "MASTERCARD"
    CardType.UNKNOWN -> ""
}
