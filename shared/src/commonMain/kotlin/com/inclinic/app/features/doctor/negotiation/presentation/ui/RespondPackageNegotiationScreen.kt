package com.inclinic.app.features.doctor.negotiation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.inclinic.app.features.doctor.negotiation.core.model.PackageNegotiation
import com.inclinic.app.features.doctor.negotiation.presentation.component.RespondPackageNegotiationComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ConfirmDialog
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RespondPackageNegotiationScreen(
    component: RespondPackageNegotiationComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing20)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack, contentDescription = "Volver")
            Text(
                text = "Contraoferta del paciente",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        val negotiation = state.negotiation
        when {
            state.isLoading && negotiation == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Cargando…", style = typography.body, color = colors.muted)
                }
            }

            negotiation == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.spacing20),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingMd, Alignment.CenterVertically),
                ) {
                    ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())
                    AppButton(
                        text = "Reintentar",
                        onClick = component::onRetry,
                        variant = AppButtonVariant.Outline,
                        size = AppButtonSize.Md,
                    )
                }
            }

            else -> NegotiationContent(
                negotiation = negotiation,
                counterPrice = state.counterPrice,
                isResponding = state.isResponding,
                error = state.error,
                onAccept = component::onAccept,
                onReject = component::onReject,
                onCounterPriceChange = component::onCounterPriceChange,
                onSubmitCounter = component::onSubmitCounter,
            )
        }
    }
}

@Composable
private fun NegotiationContent(
    negotiation: PackageNegotiation,
    counterPrice: String,
    isResponding: Boolean,
    error: String?,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCounterPriceChange: (String) -> Unit,
    onSubmitCounter: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    var showRejectConfirm by remember { mutableStateOf(false) }

    if (showRejectConfirm) {
        ConfirmDialog(
            title = "¿Rechazar esta contraoferta?",
            message = "Esta acción no se puede deshacer.",
            onConfirm = {
                showRejectConfirm = false
                onReject()
            },
            onDismiss = { showRejectConfirm = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimens.spacing20, vertical = dimens.spacingSm),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radiusLarge))
                .background(colors.navyTint)
                .padding(dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
        ) {
            Text(
                text = "PAQUETE EN NEGOCIACIÓN",
                style = typography.label,
                color = colors.navy,
                letterSpacing = 0.6.sp,
            )
            Text(text = negotiation.packageName, style = typography.subtitle, color = colors.navyDark)
            Text(text = negotiation.patientName, style = typography.caption, color = colors.navy)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radiusLarge))
                .background(colors.amberBg)
                .padding(dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Icon(Lucide.MessageCircle, contentDescription = null, tint = colors.amber, modifier = Modifier.size(16.dp))
                Text(
                    text = "${negotiation.patientName} propuso una contraoferta",
                    style = typography.label.copy(fontSize = 12.sp),
                    color = colors.amber,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
                PriceColumn(
                    title = "TU OFERTA",
                    price = formatPrice(negotiation.originalPriceCents),
                    titleColor = colors.muted,
                    valueColor = colors.text,
                    background = colors.lav50,
                    modifier = Modifier.weight(1f),
                )
                PriceColumn(
                    title = "SU CONTRAOFERTA",
                    price = formatPrice(negotiation.proposedPriceCents),
                    titleColor = colors.amber,
                    valueColor = colors.amber,
                    background = colors.amberBg,
                    modifier = Modifier.weight(1f),
                )
            }
            if (!negotiation.message.isNullOrBlank()) {
                Text(
                    text = "\"${negotiation.message}\"",
                    style = typography.caption.copy(fontStyle = FontStyle.Italic),
                    color = colors.muted,
                )
            }
        }

        Text(
            text = "TU CONTRAOFERTA",
            style = typography.label,
            color = colors.muted,
            letterSpacing = 0.8.sp,
        )
        AppTextField(
            value = counterPrice,
            onValueChange = onCounterPriceChange,
            label = "Precio en centavos",
            placeholder = "Ej. 10000",
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radiusLarge))
                .background(colors.blueBg)
                .padding(dimens.spacing12),
        ) {
            Icon(Lucide.Info, contentDescription = null, tint = colors.blue, modifier = Modifier.size(18.dp))
            Text(
                text = "Al aceptar, el paquete pasa a pago pendiente. El paciente dispone de 30 min para pagar.",
                style = typography.subtitle.copy(fontSize = 11.sp),
                color = colors.blue,
            )
        }

        ErrorBanner(message = error, modifier = Modifier.fillMaxWidth())

        AppButton(
            text = "Aceptar ${formatPrice(negotiation.proposedPriceCents)}",
            onClick = onAccept,
            loading = isResponding,
            enabled = !isResponding,
            size = AppButtonSize.Lg,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm)) {
            AppButton(
                text = "Contraofertar",
                onClick = onSubmitCounter,
                variant = AppButtonVariant.Outline,
                enabled = !isResponding,
                size = AppButtonSize.Md,
                modifier = Modifier.weight(1f),
            )
            AppButton(
                text = "Rechazar",
                onClick = { showRejectConfirm = true },
                variant = AppButtonVariant.Danger,
                enabled = !isResponding,
                size = AppButtonSize.Md,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PriceColumn(
    title: String,
    price: String,
    titleColor: androidx.compose.ui.graphics.Color,
    valueColor: androidx.compose.ui.graphics.Color,
    background: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radius))
            .background(background)
            .padding(dimens.spacingSm),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
    ) {
        Text(text = title, style = typography.label.copy(fontSize = 9.sp), color = titleColor, letterSpacing = 0.6.sp)
        Text(text = price, style = typography.subtitle.copy(fontSize = 14.sp), color = valueColor)
    }
}

private fun formatPrice(cents: Int): String {
    val units = cents / 100
    val fraction = (cents % 100).toString().padStart(2, '0')
    return "S/. $units.$fraction"
}
