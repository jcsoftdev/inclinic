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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.Ban
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.Phone
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Stethoscope
import com.composables.icons.lucide.Users
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorDetail
import com.inclinic.app.features.admin.presentation.component.AdminDoctorDetailComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ChipStatus
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDoctorDetailScreen(
    component: AdminDoctorDetailComponent,
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
                    text = state.detail?.user?.fullName ?: "Detalle Doctor",
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

            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                    modifier = Modifier.padding(dimens.spacingLg),
                ) {
                    Icon(Lucide.CircleAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(40.dp))
                    Text(state.error!!, color = colors.red, style = AppTheme.typography.body)
                }
            }

            state.detail != null -> DoctorDetailContent(
                detail = state.detail!!,
                isSuspending = state.isSuspending,
                suspendError = state.suspendError,
                onSuspend = component::onSuspend,
                onUnsuspend = component::onUnsuspend,
            )
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun DoctorDetailContent(
    detail: AdminDoctorDetail,
    isSuspending: Boolean,
    suspendError: String?,
    onSuspend: (reason: String) -> Unit,
    onUnsuspend: () -> Unit,
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
        // Profile hero card
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
                DoctorAvatar(initials = detail.user.initials, size = 48)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        detail.user.fullName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    Text(
                        detail.specialties.joinToString(" · ") { it.name },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.muted,
                    )
                }
                val (label, kind) = doctorStatusChip(detail.user.let {
                    when {
                        it.isSuspended -> "SUSPENDIDO"
                        detail.isActive -> "ACTIVO"
                        else -> "INACTIVO"
                    }
                })
                ChipStatus(label = label, kind = kind)
            }
            detail.bio?.let { bio ->
                if (bio.isNotBlank()) {
                    Text(bio, fontSize = 12.sp, color = colors.muted)
                }
            }
        }

        // Metrics row
        if (detail.rating != null || detail.appointmentCount != null) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                detail.rating?.let { rating ->
                    MetricCard(
                        icon = Lucide.Star,
                        label = "Rating",
                        value = rating.formatDecimal(1),
                        sub = "${detail.reviewCount ?: 0} reseñas",
                        modifier = Modifier.weight(1f),
                    )
                }
                detail.appointmentCount?.let { count ->
                    MetricCard(
                        icon = Lucide.Calendar,
                        label = "Citas",
                        value = count.toString(),
                        sub = "totales",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // Contact info
        DocSectionLabel("CONTACTO")
        DocSectionCard {
            detail.user.email.let { email ->
                DocInfoRow(icon = Lucide.Mail, label = "Email", value = email)
            }
            detail.user.phone?.let { phone ->
                DocInfoRow(icon = Lucide.Phone, label = "Teléfono", value = phone)
            }
            detail.cmpNumber?.let { cmp ->
                DocInfoRow(icon = Lucide.Stethoscope, label = "CMP", value = cmp)
            }
        }

        // Suspension info (backend gap: suspension metadata not in GET /api/doctors/:id — uses list item's user fields)
        if (detail.user.isSuspended) {
            DocSectionLabel("SUSPENSIÓN")
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius))
                    .background(colors.redBg)
                    .border(1.dp, colors.red, RoundedCornerShape(dimens.radius))
                    .padding(dimens.spacing12),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                ) {
                    Icon(Lucide.Ban, contentDescription = null, tint = colors.red, modifier = Modifier.size(16.dp))
                    Text("Usuario suspendido", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.red)
                }
                detail.user.suspensionReason?.let { reason ->
                    Text(reason, fontSize = 12.sp, color = colors.muted)
                }
                detail.user.suspendedAt?.let { at ->
                    Text("Fecha: $at", fontSize = 11.sp, color = colors.muted)
                }
            }
        }

        // Suspend / unsuspend actions — wired to SuspendUserUseCase / UnsuspendUserUseCase
        // via AdminDoctorDetailComponent.onSuspend / onUnsuspend (detail.user.id, not detail.id).
        DocSectionLabel("ACCIONES")
        DoctorSuspendActionsCard(
            isSuspended = detail.user.isSuspended,
            isSuspending = isSuspending,
            suspendError = suspendError,
            onSuspend = onSuspend,
            onUnsuspend = onUnsuspend,
        )

        Spacer(Modifier.height(dimens.spacingLg))
    }
}

/**
 * Suspend/reactivate action card.
 * - Active doctor: reveals an inline reason field (>= 10 chars, mirrors the backend minimum
 *   enforced by [com.inclinic.app.features.admin.presentation.component.AdminSuspendUserState.canSubmit])
 *   before enabling "Suspender".
 * - Suspended doctor: single "Reactivar" button, no reason required (matches UnsuspendUserUseCase).
 */
@Composable
private fun DoctorSuspendActionsCard(
    isSuspended: Boolean,
    isSuspending: Boolean,
    suspendError: String?,
    onSuspend: (reason: String) -> Unit,
    onUnsuspend: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    var reason by remember { mutableStateOf("") }
    val bg = if (isSuspended) colors.greenBg else colors.redBg
    val border = if (isSuspended) colors.green else colors.red

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(dimens.radius))
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        if (isSuspended) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Icon(Lucide.Ban, contentDescription = null, tint = colors.green, modifier = Modifier.size(14.dp))
                Text(
                    "Este doctor está suspendido — puede reactivar su cuenta.",
                    fontSize = 11.sp,
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
            }
            AppButton(
                text = "Reactivar doctor",
                onClick = onUnsuspend,
                loading = isSuspending,
                enabled = !isSuspending,
                variant = AppButtonVariant.Navy,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Icon(Lucide.CircleAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(14.dp))
                Text(
                    "Suspender bloquea el acceso del doctor a la plataforma.",
                    fontSize = 11.sp,
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
            }
            AppTextField(
                value = reason,
                onValueChange = { reason = it },
                label = "Motivo de suspensión",
                placeholder = "Describe el motivo (mín. 10 caracteres)",
                enabled = !isSuspending,
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Suspender doctor",
                onClick = { onSuspend(reason.trim()) },
                loading = isSuspending,
                enabled = !isSuspending && reason.trim().length >= 10,
                variant = AppButtonVariant.Danger,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        suspendError?.let {
            Text(it, fontSize = 11.sp, color = colors.red)
        }
    }
}

@Composable
private fun DoctorAvatar(initials: String, size: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(colors.navyTint),
    ) {
        Text(
            text = initials,
            color = colors.navy,
            fontSize = (size * 0.35).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Column(
        modifier
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.navy, modifier = Modifier.size(16.dp))
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = colors.text)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.muted)
        Text(sub, fontSize = 10.sp, color = colors.light)
    }
}

@Composable
private fun DocSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = AppTheme.colors.muted,
    )
}

@Composable
private fun DocSectionCard(content: @Composable () -> Unit) {
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
private fun DocInfoRow(icon: ImageVector, label: String, value: String) {
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
