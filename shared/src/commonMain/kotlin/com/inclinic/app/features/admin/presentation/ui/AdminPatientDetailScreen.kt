package com.inclinic.app.features.admin.presentation.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mail
import com.composables.icons.lucide.Phone
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import com.inclinic.app.features.admin.presentation.component.AdminPatientDetailComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.molecules.KpiCard
import com.inclinic.app.ui.theme.AppTheme

/**
 * Admin patient detail screen.
 *
 * Data shown: name, tier, isSuspended badge, email, phone (if present),
 * appointmentCount, therapyPackageCount, lastLoginLabel.
 *
 * Omitted (no by-id endpoint, not in list payload):
 * - LTV / gasto total (not in patients-list response)
 * - City / location
 * - "Verified" flag (no verification field on patient model)
 * - Appointment history list (out of scope; no endpoint)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPatientDetailScreen(component: AdminPatientDetailComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val patient = state.patient
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    "Detalle Paciente",
                    style = AppTheme.typography.displayXSmall,
                    fontSize = 20.sp,
                    color = colors.text,
                )
            },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            actions = {
                // Edit icon — non-functional (no edit-patient endpoint); kept as visual affordance
                Icon(
                    Lucide.Pencil,
                    contentDescription = "Editar (no disponible)",
                    tint = colors.light,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(20.dp),
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimens.spacingMd),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingMd),
        ) {
            // Hero card
            HeroCard(patient = patient)

            // Metric cards
            MetricsRow(patient = patient)

            // Contact section
            ContactSection(patient = patient)

            // Reactivate error
            if (state.reactivateError != null) {
                ErrorBanner(message = state.reactivateError)
            }

            // Actions section
            ActionsSection(
                isSuspended = patient.isSuspended,
                isReactivating = state.isReactivating,
                onSuspend = component::onSuspend,
                onReactivate = component::onReactivate,
            )

            Spacer(Modifier.height(dimens.spacingLg))
        }
    }
}

@Composable
private fun HeroCard(patient: AdminPatientListItem) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacingMd),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        // Initials avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.navyTint),
        ) {
            Text(
                text = patient.initials,
                color = colors.navy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Text(
            text = patient.fullName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = patient.tierLabel,
                fontSize = 12.sp,
                color = colors.muted,
            )
            if (patient.isSuspended) {
                AppBadge(text = "Suspendido", tone = AppBadgeTone.Error)
            } else {
                AppBadge(text = "Activo", tone = AppBadgeTone.Success)
            }
        }

        patient.lastLoginLabel?.let { label ->
            Text(
                text = "Último acceso $label",
                fontSize = 11.sp,
                color = colors.light,
            )
        }
    }
}

@Composable
private fun MetricsRow(patient: AdminPatientListItem) {
    val dimens = AppTheme.dimens
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        KpiCard(
            label = "Citas",
            value = patient.appointmentCount.toString(),
            modifier = Modifier.weight(1f),
        )
        KpiCard(
            label = "Paquetes",
            value = patient.therapyPackageCount.toString(),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ContactSection(patient: AdminPatientListItem) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
    ) {
        Text(
            "CONTACTO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.muted,
        )
        ContactRow(icon = Lucide.Mail, text = patient.email)
        patient.phone?.let { phone ->
            ContactRow(icon = Lucide.Phone, text = phone)
        }
    }
}

@Composable
private fun ContactRow(icon: ImageVector, text: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.navy, modifier = Modifier.size(16.dp))
        Text(text = text, fontSize = 13.sp, color = colors.text)
    }
}

@Composable
private fun ActionsSection(
    isSuspended: Boolean,
    isReactivating: Boolean,
    onSuspend: () -> Unit,
    onReactivate: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacingMd),
        verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
    ) {
        Text(
            "ACCIONES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.muted,
        )
        if (isSuspended) {
            AppButton(
                text = "Reactivar cuenta",
                onClick = onReactivate,
                variant = AppButtonVariant.Outline,
                size = AppButtonSize.Md,
                loading = isReactivating,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            AppButton(
                text = "Suspender cuenta",
                onClick = onSuspend,
                variant = AppButtonVariant.Danger,
                size = AppButtonSize.Md,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        // Historial de citas: no endpoint to fetch by patient ID in this scope.
        // TODO: wire to admin appointments filtered by patientId when backend supports it.
        AppButton(
            text = "Historial de citas",
            onClick = { /* TODO: navigate to appointments filtered by this patient */ },
            variant = AppButtonVariant.Ghost,
            size = AppButtonSize.Md,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
