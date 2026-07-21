package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.HandCoins
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.Stethoscope
import com.composables.icons.lucide.User
import com.inclinic.app.core.util.DetailLoadState
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentDetail
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentPerson
import com.inclinic.app.features.admin.presentation.component.AdminAppointmentDetailComponent
import com.inclinic.app.features.admin.presentation.component.toDetailLoadState
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.atoms.DetailErrorState
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentDetailScreen(
    component: AdminAppointmentDetailComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    text = state.detail?.let { "Cita #${it.id.takeLast(6).uppercase()}" } ?: "Detalle Cita",
                    style = AppTheme.typography.titleLarge,
                    color = colors.text,
                )
            },
            navigationIcon = {
                AppBackButton(onClick = component::onBack)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        when (val loadState = state.toDetailLoadState()) {
            is DetailLoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }

            is DetailLoadState.NotFound -> DetailErrorState(
                message = loadState.message,
                onBackToList = component::onBack,
                notFound = true,
            )

            is DetailLoadState.Failed -> DetailErrorState(
                message = loadState.message,
                onBackToList = component::onBack,
            )

            is DetailLoadState.Content -> DetailContent(
                detail = loadState.value,
                onResolveDispute = component::onNavigateToResolveDispute,
            )
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun DetailContent(
    detail: AdminAppointmentDetail,
    onResolveDispute: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
    ) {
        // Status banner for held/disputed appointments
        if (!detail.paymentHoldStatus.isNullOrBlank() || detail.hasDispute) {
            StatusBannerCard(detail = detail)
        }

        // Status chip + specialty row
        SectionCard {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Icon(Lucide.Calendar, contentDescription = null, tint = colors.muted, modifier = Modifier.size(16.dp))
                Text(
                    formatStartTime(detail.startTime),
                    fontSize = 13.sp,
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                val (label, kind) = appointmentStatusChip(detail.status)
                ChipStatus(label = label, kind = kind)
            }
            Spacer(Modifier.height(2.dp))
            InfoRow(icon = Lucide.Stethoscope, label = "Especialidad", value = detail.specialty.name)
            detail.notes?.let { notes ->
                if (notes.isNotBlank()) InfoRow(icon = Lucide.CircleAlert, label = "Notas", value = notes)
            }
            if (detail.rescheduleCount > 0) {
                InfoRow(icon = Lucide.Calendar, label = "Reagendamientos", value = detail.rescheduleCount.toString())
            }
        }

        // Participants
        SectionLabel("PARTICIPANTES")
        ParticipantCard(icon = Lucide.User, role = "Paciente", person = detail.patient)
        ParticipantCard(icon = Lucide.Stethoscope, role = "Doctor", person = detail.doctor)

        // Payment
        SectionLabel("PAGO")
        SectionCard {
            InfoRow(
                icon = Lucide.HandCoins,
                label = "Precio",
                value = "S/ ${detail.price.formatDecimal(2)}",
            )
            detail.commission?.let { commission ->
                InfoRow(
                    icon = Lucide.HandCoins,
                    label = "Comisión plataforma",
                    value = "S/ ${commission.formatDecimal(2)}",
                )
            }
            InfoRow(icon = Lucide.HandCoins, label = "Estado pago", value = detail.paymentStatus)
            detail.paymentHoldStatus?.let { hold ->
                if (hold.isNotBlank()) {
                    InfoRow(icon = Lucide.HandCoins, label = "Hold", value = hold)
                }
            }
        }

        // Dispute section
        if (detail.hasDispute) {
            SectionLabel("DISPUTA")
            SectionCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                ) {
                    Icon(Lucide.ShieldAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(16.dp))
                    Text("Estado:", fontSize = 12.sp, color = colors.muted)
                    AppBadge(text = detail.disputeStatus ?: "DISPUTE", tone = AppBadgeTone.Error)
                }
                detail.disputeReason?.let { reason ->
                    if (reason.isNotBlank()) {
                        Text(reason, fontSize = 12.sp, color = colors.muted, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                Spacer(Modifier.height(dimens.spacingSm))
                AppButton(
                    text = "Resolver disputa",
                    onClick = onResolveDispute,
                    variant = AppButtonVariant.Navy,
                )
            }
        }

        Spacer(Modifier.height(dimens.spacingLg))
    }
}

@Composable
private fun StatusBannerCard(detail: AdminAppointmentDetail) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val isDispute = detail.hasDispute
    val bgColor = if (isDispute) colors.redBg else colors.amberBg
    val borderColor = if (isDispute) colors.red else colors.amber
    val icon = if (isDispute) Lucide.ShieldAlert else Lucide.CircleAlert
    val iconTint = if (isDispute) colors.red else colors.amber

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(dimens.radius))
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (isDispute) "Disputa abierta" else "Pago retenido",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            val subtitle = when {
                isDispute -> detail.disputeReason ?: "Revisión requerida por el administrador."
                else -> "Estado de retención: ${detail.paymentHoldStatus}"
            }
            Text(subtitle, fontSize = 12.sp, color = colors.muted)
        }
    }
}

@Composable
private fun ParticipantCard(icon: ImageVector, role: String, person: AdminAppointmentPerson) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(dimens.radiusMd))
                .background(colors.navyTint),
        ) {
            Icon(icon, contentDescription = null, tint = colors.navy, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = person.fullName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Text(
                text = "$role · ${person.email}",
                fontSize = 11.sp,
                color = colors.muted,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = AppTheme.colors.muted,
    )
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        content()
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingSm),
    ) {
        Icon(icon, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
        Text(
            text = "$label:",
            fontSize = 12.sp,
            color = colors.muted,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.4f),
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = colors.text,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.6f),
        )
    }
}
