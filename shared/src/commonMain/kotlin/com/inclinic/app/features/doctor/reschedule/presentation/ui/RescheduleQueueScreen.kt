package com.inclinic.app.features.doctor.reschedule.presentation.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.AlarmClock
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequest
import com.inclinic.app.features.doctor.reschedule.core.model.RescheduleRequestStatus
import com.inclinic.app.features.doctor.reschedule.presentation.component.RescheduleQueueComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.ConfirmDialog
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun RescheduleQueueScreen(
    component: RescheduleQueueComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    var rejectTargetId by remember { mutableStateOf<String?>(null) }

    rejectTargetId?.let { targetId ->
        ConfirmDialog(
            title = "¿Rechazar esta solicitud de reagenda?",
            message = "Esta acción no se puede deshacer.",
            onConfirm = {
                rejectTargetId = null
                component.onReject(targetId)
            },
            onDismiss = { rejectTargetId = null },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd)
                .height(52.dp),
        ) {
            AppBackButton(onClick = component::onBack)
            Text(
                text = "Cola de Reagendas",
                style = typography.titleLarge,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
        ) {
            ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())

            val pending = state.requests.count { it.status == RescheduleRequestStatus.PENDING }
            if (pending > 0) {
                PendingBanner(count = pending)
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().padding(top = dimens.spacingXl), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
            } else if (state.requests.isEmpty()) {
                EmptyCard()
            } else {
                state.requests.forEach { req ->
                    RescheduleCard(
                        request = req,
                        responding = state.respondingId == req.id,
                        onApprove = { component.onApprove(req.id) },
                        onReject = { rejectTargetId = req.id },
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingBanner(count: Int) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius))
            .background(colors.amberBg)
            .border(1.dp, colors.amber, RoundedCornerShape(dimens.radius))
            .padding(14.dp),
    ) {
        Icon(Lucide.AlarmClock, contentDescription = null, tint = colors.amber, modifier = Modifier.size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Tienes $count ${if (count == 1) "solicitud pendiente" else "solicitudes pendientes"}",
                color = colors.amber,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Responde antes de 24h o se cerrarán automáticamente.",
                color = colors.amber,
                style = AppTheme.typography.caption,
            )
        }
    }
}

@Composable
private fun EmptyCard() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(AppTheme.colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacingXl),
    ) {
        Text(
            text = "No tienes solicitudes de reagenda.",
            style = AppTheme.typography.subtitle,
            color = colors.muted,
        )
    }
}

@Composable
private fun RescheduleCard(
    request: RescheduleRequest,
    responding: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    val (chipLabel, chipKind) = statusChip(request.status)

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(AppTheme.colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.sand),
                ) {
                    Text(
                        text = initials(request.patientName),
                        color = colors.navy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = request.patientName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                )
            }
            if (request.status == RescheduleRequestStatus.PENDING) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(dimens.radiusChip))
                        .background(colors.sand)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = request.dateLabel ?: request.currentSlot,
                        color = colors.muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                ChipStatus(label = chipLabel, kind = chipKind)
            }
        }

        request.reason?.let { reason ->
            Text(
                text = "Motivo: \"$reason\"",
                fontSize = 13.sp,
                fontStyle = FontStyle.Italic,
                color = colors.muted,
            )
        }

        if (request.status == RescheduleRequestStatus.PENDING) {
            Row(horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm), modifier = Modifier.fillMaxWidth()) {
                AppButton(
                    text = "Aceptar",
                    onClick = onApprove,
                    variant = AppButtonVariant.Navy,
                    size = AppButtonSize.Sm,
                    loading = responding,
                    enabled = !responding,
                    modifier = Modifier.weight(1f),
                )
                AppButton(
                    text = "Rechazar",
                    onClick = onReject,
                    variant = AppButtonVariant.Outline,
                    size = AppButtonSize.Sm,
                    enabled = !responding,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private fun initials(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

private fun statusChip(status: RescheduleRequestStatus): Pair<String, ChipStatusKind> = when (status) {
    RescheduleRequestStatus.PENDING -> "PENDIENTE" to ChipStatusKind.Warning
    RescheduleRequestStatus.APPROVED -> "APROBADO" to ChipStatusKind.Success
    RescheduleRequestStatus.REJECTED -> "RECHAZADO" to ChipStatusKind.Error
    RescheduleRequestStatus.EXPIRED -> "EXPIRADA" to ChipStatusKind.Neutral
    RescheduleRequestStatus.UNKNOWN -> "—" to ChipStatusKind.Neutral
}
