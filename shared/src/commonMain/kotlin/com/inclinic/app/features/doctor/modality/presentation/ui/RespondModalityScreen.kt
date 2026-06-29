package com.inclinic.app.features.doctor.modality.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowRight
import com.composables.icons.lucide.Banknote
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Video
import com.inclinic.app.features.doctor.modality.core.model.ModalityChangeRequest
import com.inclinic.app.features.doctor.modality.core.model.ModalityRequestStatus
import com.inclinic.app.features.doctor.modality.presentation.component.RespondModalityComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RespondModalityScreen(
    component: RespondModalityComponent,
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
                text = "Cambio de modalidad",
                style = typography.titleLarge,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
            }

            state.request == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.spacing20, vertical = dimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
                ) {
                    ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())
                    AppButton(
                        text = "Reintentar",
                        onClick = component::onRetry,
                        variant = AppButtonVariant.Navy,
                        size = AppButtonSize.Md,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            else -> {
                val request = state.request!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dimens.spacing20, vertical = dimens.spacingSm),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
                ) {
                    PatientCard(request)
                    ModalityCard(request)

                    if (request.status == ModalityRequestStatus.PENDING) {
                        val suggested = request.suggestedPrice?.let { "S/$it sugerido" } ?: ""
                        AppTextField(
                            value = state.adjustedPrice,
                            onValueChange = component::onPriceChange,
                            label = "AJUSTAR PRECIO (OPCIONAL)",
                            placeholder = suggested,
                            singleLine = true,
                            leadingIcon = {
                                Icon(Lucide.Banknote, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        request.suggestedPrice?.let { price ->
                            InfoBanner(
                                title = "Precio sugerido domicilio: S/$price",
                                description = "Puedes ajustarlo según tu tarifa. El paciente verá el monto antes de aceptar.",
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())

                    if (request.status == ModalityRequestStatus.PENDING) {
                        AppButton(
                            text = "Aceptar cambio",
                            onClick = component::onApprove,
                            loading = state.isResponding,
                            enabled = !state.isResponding,
                            variant = AppButtonVariant.Navy,
                            size = AppButtonSize.Lg,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        AppButton(
                            text = "Rechazar solicitud",
                            onClick = component::onReject,
                            loading = state.isResponding,
                            enabled = !state.isResponding,
                            variant = AppButtonVariant.Outline,
                            size = AppButtonSize.Lg,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientCard(request: ModalityChangeRequest) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    val (chipLabel, chipKind) = statusChip(request.status)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.blueBg),
        ) {
            Text(
                text = initials(request.patientName),
                color = colors.blue,
                style = typography.subtitle,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(request.patientName, style = typography.subtitle, fontWeight = FontWeight.Bold, color = colors.text)
            Text(request.patientSubtitle ?: request.appointmentSlot, style = typography.caption, color = colors.muted)
        }
        if (request.status != ModalityRequestStatus.PENDING) {
            ChipStatus(label = chipLabel, kind = chipKind)
        }
    }
}

@Composable
private fun ModalityCard(request: ModalityChangeRequest) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.elevated)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Icon(Lucide.CalendarClock, contentDescription = null, tint = colors.navy, modifier = Modifier.size(16.dp))
            Text("Cita ${request.appointmentSlot}", style = typography.subtitle, fontWeight = FontWeight.Bold, color = colors.text)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius))
                .background(colors.sand)
                .padding(10.dp),
        ) {
            ModalityColumn(
                label = "ACTUAL",
                value = request.currentModality,
                labelColor = colors.muted,
                valueColor = colors.text,
                modifier = Modifier.weight(1f),
            )
            Icon(Lucide.ArrowRight, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
            ModalityColumn(
                label = "SOLICITADO",
                value = request.requestedModality,
                labelColor = colors.navy,
                valueColor = colors.navy,
                modifier = Modifier.weight(1f),
            )
        }

        request.reason?.let { reason ->
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
                Text(
                    text = "MOTIVO DEL PACIENTE",
                    style = typography.label,
                    color = colors.muted,
                    letterSpacing = 0.6.sp,
                )
                Text(
                    text = "\"$reason\"",
                    style = typography.subtitle,
                    color = colors.text,
                    fontStyle = FontStyle.Italic,
                )
            }
        }

        request.address?.let { address ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius))
                    .background(colors.navyTint)
                    .padding(10.dp),
            ) {
                Icon(Lucide.MapPin, contentDescription = null, tint = colors.navy, modifier = Modifier.size(16.dp))
                Text(address, style = typography.caption, fontWeight = FontWeight.SemiBold, color = colors.navy)
            }
        }
    }
}

@Composable
private fun ModalityColumn(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    val typography = AppTheme.typography
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier,
    ) {
        Icon(modalityIcon(value), contentDescription = null, tint = valueColor, modifier = Modifier.size(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = typography.label, color = labelColor, letterSpacing = 0.6.sp)
            Text(value, style = typography.subtitle, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

private fun modalityIcon(value: String): ImageVector = when {
    value.contains("virtual", ignoreCase = true) || value.contains("video", ignoreCase = true) -> Lucide.Video
    value.contains("domicilio", ignoreCase = true) || value.contains("casa", ignoreCase = true) -> Lucide.House
    else -> Lucide.Building2
}

private fun initials(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

private fun statusChip(status: ModalityRequestStatus): Pair<String, ChipStatusKind> = when (status) {
    ModalityRequestStatus.PENDING -> "PENDIENTE" to ChipStatusKind.Warning
    ModalityRequestStatus.APPROVED -> "APROBADO" to ChipStatusKind.Success
    ModalityRequestStatus.REJECTED -> "RECHAZADO" to ChipStatusKind.Error
    ModalityRequestStatus.EXPIRED -> "EXPIRADA" to ChipStatusKind.Neutral
    ModalityRequestStatus.UNKNOWN -> "DESCONOCIDO" to ChipStatusKind.Neutral
}
