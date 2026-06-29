package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldAlert
import com.inclinic.app.features.admin.presentation.component.AdminSuspendUserComponent
import com.inclinic.app.features.admin.presentation.component.SuspendReason
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSuspendUserScreen(component: AdminSuspendUserComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
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
                    "Suspender usuario",
                    style = AppTheme.typography.displayXSmall,
                    fontSize = 20.sp,
                    color = colors.text,
                )
            },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            actions = {
                Icon(
                    Lucide.ShieldAlert,
                    contentDescription = "Acción sensible",
                    tint = colors.red,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(22.dp),
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
            // Danger banner
            InfoBanner(
                title = "Acción sensible",
                description = "La cuenta perderá acceso y se detendrán reservas nuevas.",
                tone = InfoBannerTone.Error,
            )

            // Patient identification row
            PatientIdentityRow(
                name = state.patient.fullName,
                email = state.patient.email,
            )

            // Reason section
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radiusLarge))
                    .background(colors.surface)
                    .padding(dimens.spacingMd),
                verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            ) {
                Text(
                    "MOTIVO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.muted,
                )

                SuspendReason.entries.forEach { reason ->
                    ReasonOptionCard(
                        reason = reason,
                        isSelected = state.selectedReason == reason,
                        onClick = { component.onReasonSelected(reason) },
                    )
                }

                // Free-text input — only visible when "Otro" is selected
                if (state.selectedReason == SuspendReason.Other) {
                    AppTextField(
                        value = state.freeText,
                        onValueChange = component::onFreeTextChange,
                        label = "Detalle del motivo",
                        placeholder = "Describe el motivo de la suspensión...",
                    )
                }
            }

            // Error banner
            ErrorBanner(message = state.error)

            Spacer(Modifier.height(dimens.spacingSm))

            // Submit button
            AppButton(
                text = "Continuar",
                onClick = component::onSubmit,
                variant = AppButtonVariant.Danger,
                size = AppButtonSize.Lg,
                loading = state.isSubmitting,
                enabled = state.canSubmit,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(dimens.spacingLg))
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun PatientIdentityRow(name: String, email: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Text(
                text = email,
                fontSize = 12.sp,
                color = colors.muted,
            )
        }
    }
}

@Composable
private fun ReasonOptionCard(
    reason: SuspendReason,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    val borderColor = if (isSelected) colors.navy else colors.border
    val bgColor = if (isSelected) colors.navyTint else Color.Transparent

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(dimens.radius))
            .clickable(onClick = onClick)
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        if (isSelected) {
            Icon(
                imageVector = Lucide.CircleCheck,
                contentDescription = null,
                tint = colors.navy,
                modifier = Modifier.size(20.dp),
            )
        } else {
            // Unselected — empty circle indicator
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, colors.light, CircleShape),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = reason.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Text(
                text = reason.detail,
                fontSize = 11.sp,
                color = colors.muted,
            )
        }
    }
}
