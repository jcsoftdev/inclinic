package com.inclinic.app.features.doctor.profile.presentation.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.doctor.profile.core.model.MySpecialtyRequest
import com.inclinic.app.features.doctor.profile.core.model.SpecialtyRequestStatus
import com.inclinic.app.features.doctor.profile.presentation.component.MySpecialtyRequestsComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.ChipStatusKind
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.LoadingOverlay
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun MySpecialtyRequestsScreen(
    component: MySpecialtyRequestsComponent,
    modifier: Modifier = Modifier,
) {
    AppTheme {
        val state by component.state.subscribeAsState()
        val colors = AppTheme.colors
        val dimens = AppTheme.dimens
        val typography = AppTheme.typography

        Box(modifier = modifier.fillMaxSize().background(colors.sand)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surface)
                        .height(52.dp)
                        .padding(horizontal = dimens.spacingMd),
                ) {
                    AppBackButton(onClick = component::onBack)
                    Text(
                        text = "Mis especialidades",
                        style = typography.displayNano,
                        color = colors.text,
                        modifier = Modifier.weight(1f),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
                ) {
                    ErrorBanner(message = state.error, modifier = Modifier.fillMaxWidth())

                    if (state.error != null) {
                        AppButton(
                            text = "Reintentar",
                            onClick = component::onRetry,
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                        )
                    } else if (state.requests.isEmpty() && !state.isLoading) {
                        EmptyState()
                    } else {
                        state.requests.forEach { request ->
                            RequestCard(request)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
                ) {
                    AppButton(
                        text = "Solicitar nueva especialidad",
                        onClick = component::onRequestNew,
                        size = AppButtonSize.Lg,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            LoadingOverlay(visible = state.isLoading)
        }
    }
}

private fun SpecialtyRequestStatus.chipKind(): ChipStatusKind = when (this) {
    SpecialtyRequestStatus.Pending -> ChipStatusKind.Warning
    SpecialtyRequestStatus.Approved -> ChipStatusKind.Success
    SpecialtyRequestStatus.Rejected -> ChipStatusKind.Error
    SpecialtyRequestStatus.Expired -> ChipStatusKind.Neutral
    SpecialtyRequestStatus.Unknown -> ChipStatusKind.Neutral
}

private fun SpecialtyRequestStatus.label(): String = when (this) {
    SpecialtyRequestStatus.Pending -> "PENDIENTE"
    SpecialtyRequestStatus.Approved -> "APROBADA"
    SpecialtyRequestStatus.Rejected -> "RECHAZADA"
    SpecialtyRequestStatus.Expired -> "EXPIRADA"
    SpecialtyRequestStatus.Unknown -> "DESCONOCIDA"
}

private fun SpecialtyRequestStatus.prefix(): String = when (this) {
    SpecialtyRequestStatus.Pending -> "Enviada"
    SpecialtyRequestStatus.Approved -> "Aprobada"
    SpecialtyRequestStatus.Rejected -> "Rechazada"
    SpecialtyRequestStatus.Expired -> "Vencida"
    SpecialtyRequestStatus.Unknown -> "Enviada"
}

@Composable
private fun RequestCard(request: MySpecialtyRequest) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    val borderColor =
        if (request.status == SpecialtyRequestStatus.Rejected) colors.red else colors.border

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(dimens.radiusLarge))
            .border(dimens.borderWidth, borderColor, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacingMd),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacingXs)) {
                Text(
                    text = request.specialtyName,
                    color = colors.text,
                    style = typography.body,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${request.status.prefix()} ${request.createdAt}",
                    color = colors.muted,
                    style = typography.subtitle,
                )
            }
            ChipStatus(label = request.status.label(), kind = request.status.chipKind())
        }

        if (request.documentCount > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
            ) {
                Icon(
                    imageVector = Lucide.FileText,
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = "${request.documentCount} documentos",
                    color = colors.muted,
                    style = typography.subtitle,
                )
            }
        }

        val reason = request.rejectionReason
        if (request.status == SpecialtyRequestStatus.Rejected && reason != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimens.spacingXs),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.redBg, RoundedCornerShape(dimens.radius))
                    .padding(dimens.spacing12),
            ) {
                Text(
                    text = "MOTIVO DEL ADMIN",
                    color = colors.red,
                    style = typography.caption,
                )
                Text(
                    text = reason,
                    color = colors.red,
                    style = typography.subtitle,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface, RoundedCornerShape(dimens.radiusLarge))
            .border(dimens.borderWidth, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacingXl),
    ) {
        Text(
            text = "Aún no tienes solicitudes",
            color = colors.text,
            style = typography.body,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Tus solicitudes de especialidad aparecerán aquí.",
            color = colors.muted,
            style = typography.subtitle,
        )
    }
}
