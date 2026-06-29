package com.inclinic.app.features.doctor.sharing.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequest
import com.inclinic.app.features.doctor.sharing.core.model.ShareRequestStatus
import com.inclinic.app.features.doctor.sharing.presentation.component.ShareRequestsListComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun ShareRequestsScreen(
    component: ShareRequestsListComponent,
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
        // Header
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
                text = "Compartir Historia",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(dimens.radiusXl))
                    .background(colors.navy)
                    .clickable(onClick = component::onRequestNew),
            ) {
                Icon(Lucide.Plus, contentDescription = "Nueva solicitud", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        // Filter pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacingMd, vertical = dimens.spacingSm),
        ) {
            FilterPill("Aprobadas", state.showIncoming, component::onSelectIncoming)
            FilterPill("Pendientes", !state.showIncoming, component::onSelectOutgoing)
        }

        state.error?.let {
            Text(
                it,
                color = colors.red,
                style = typography.subtitle,
                modifier = Modifier.padding(horizontal = dimens.spacingMd),
            )
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
        } else {
            val requests = if (state.showIncoming) state.incomingRequests else state.outgoingRequests
            if (requests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sin solicitudes",
                        style = typography.subtitle,
                        color = colors.muted,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                ) {
                    items(requests, key = { it.id }) { req ->
                        ShareRequestCard(
                            request = req,
                            onCancel = { component.onCancel(req.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = label,
        color = if (selected) Color.White else colors.muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(AppTheme.dimens.radiusPill))
            .background(if (selected) colors.navy else colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun ShareRequestCard(
    request: ShareRequest,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    val (chipLabel, chipKind) = statusChip(request.status)

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(dimens.radiusLarge), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.lav),
            ) {
                Text(
                    text = initials(request.patientName),
                    color = Color.White,
                    style = typography.subtitle,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(request.patientName, style = typography.subtitle, fontWeight = FontWeight.Bold, color = colors.text)
                Text(request.reason, style = typography.subtitle, color = colors.muted, maxLines = 1)
            }
            ChipStatus(label = chipLabel, kind = chipKind)
        }

        if (request.status == ShareRequestStatus.PENDING) {
            AppButton(
                text = "Cancelar solicitud",
                onClick = onCancel,
                variant = AppButtonVariant.Outline,
                size = AppButtonSize.Sm,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun initials(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

private fun statusChip(status: ShareRequestStatus): Pair<String, ChipStatusKind> = when (status) {
    ShareRequestStatus.PENDING -> "PENDIENTE" to ChipStatusKind.Warning
    ShareRequestStatus.APPROVED -> "APROBADO" to ChipStatusKind.Success
    ShareRequestStatus.REJECTED -> "RECHAZADO" to ChipStatusKind.Error
    ShareRequestStatus.EXPIRED -> "EXPIRADA" to ChipStatusKind.Neutral
    ShareRequestStatus.REVOKED -> "REVOCADO" to ChipStatusKind.Neutral
}
