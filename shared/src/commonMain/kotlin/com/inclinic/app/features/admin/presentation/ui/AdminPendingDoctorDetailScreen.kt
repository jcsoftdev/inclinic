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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.Stethoscope
import com.composables.icons.lucide.X
import com.inclinic.app.features.admin.infrastructure.remote.AdminPendingDoctor
import com.inclinic.app.features.admin.presentation.component.AdminPendingDoctorDetailComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPendingDoctorDetailScreen(
    component: AdminPendingDoctorDetailComponent,
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
                    text = state.doctor?.fullName ?: "Solicitud pendiente",
                    style = AppTheme.typography.titleLarge,
                    color = colors.text,
                )
            },
            navigationIcon = {
                AppBackButton(onClick = component::onBack)
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }

            state.error != null && state.doctor == null -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                    modifier = Modifier.padding(dimens.spacingLg),
                ) {
                    Icon(Lucide.CircleAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(40.dp))
                    Text(state.error!!, color = colors.red, style = AppTheme.typography.body)
                }
            }

            state.doctor != null -> PendingDoctorDetailContent(
                doctor = state.doctor!!,
                rejectReason = state.rejectReason,
                rejectError = state.rejectError,
                actionError = state.error,
                isSubmitting = state.isSubmitting,
                onReasonChange = component::onReasonChange,
                onApprove = component::onApprove,
                onConfirmReject = component::onConfirmReject,
            )
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun PendingDoctorDetailContent(
    doctor: AdminPendingDoctor,
    rejectReason: String,
    rejectError: String?,
    actionError: String?,
    isSubmitting: Boolean,
    onReasonChange: (String) -> Unit,
    onApprove: () -> Unit,
    onConfirmReject: () -> Unit,
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
        // Profile card
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radiusLarge))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
                .padding(dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PendingDetailAvatar(initials = doctor.initials, size = 48)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        doctor.fullName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    Text(
                        "${doctor.primarySpecialty} · CMP: ${doctor.cmpNumber ?: "—"} · ${doctor.documentCount} documentos",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.muted,
                    )
                }
            }
            doctor.bio?.let { bio ->
                if (bio.isNotBlank()) Text(bio, fontSize = 12.sp, color = colors.muted)
            }
        }

        // Documents section
        PendingSectionLabel("DOCUMENTOS")
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radiusLarge))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
                .padding(dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            PendingInfoRow(icon = Lucide.FileText, label = "CMP", value = doctor.cmpNumber ?: "No proporcionado")
            PendingInfoRow(icon = Lucide.Mail, label = "Email", value = doctor.user.email)
            PendingInfoRow(icon = Lucide.Stethoscope, label = "Especialidad", value = doctor.primarySpecialty)
            PendingInfoRow(icon = Lucide.FileText, label = "Docs subidos", value = "${doctor.documentCount}")
        }

        // Action error banner
        actionError?.let { err ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius))
                    .background(colors.redBg)
                    .border(1.dp, colors.red, RoundedCornerShape(dimens.radius))
                    .padding(dimens.spacing12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Icon(Lucide.CircleAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(14.dp))
                Text(err, color = colors.red, style = AppTheme.typography.body)
            }
        }

        // Decision section
        PendingSectionLabel("DECISIÓN")

        // Approve
        AppButton(
            text = "Aprobar doctor",
            onClick = onApprove,
            variant = AppButtonVariant.Navy,
            loading = isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        )

        // Reject with reason field
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radiusLarge))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
                .padding(dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
            ) {
                Icon(Lucide.X, contentDescription = null, tint = colors.red, modifier = Modifier.size(14.dp))
                Text(
                    "Rechazar solicitud",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
            }
            AppTextField(
                value = rejectReason,
                onValueChange = onReasonChange,
                label = "Motivo del rechazo",
                placeholder = "Mín. 10 caracteres...",
                error = rejectError,
                singleLine = false,
            )
            AppButton(
                text = "Confirmar rechazo",
                onClick = onConfirmReject,
                variant = AppButtonVariant.Danger,
                loading = isSubmitting,
                enabled = rejectReason.length >= 10,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(dimens.spacingLg))
    }
}

@Composable
private fun PendingDetailAvatar(initials: String, size: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(colors.lav.copy(alpha = 0.25f)),
    ) {
        Text(
            text = initials,
            color = colors.lav,
            fontSize = (size * 0.35).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun PendingSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = AppTheme.colors.muted,
    )
}

@Composable
private fun PendingInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
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
            modifier = Modifier.weight(0.35f),
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = colors.text,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.65f),
        )
    }
}
