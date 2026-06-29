package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleAlert
import com.composables.icons.lucide.HandCoins
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCcw
import com.inclinic.app.features.admin.infrastructure.remote.AdminDisputeItem
import com.inclinic.app.features.admin.presentation.component.AdminResolveDisputeComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminResolveDisputeScreen(
    component: AdminResolveDisputeComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    "Disputa #${state.dispute?.id?.takeLast(4) ?: "…"}",
                    style = AppTheme.typography.titleLarge,
                    color = colors.text,
                )
            },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
            state.loadError != null && state.dispute == null -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spacingSm),
                    modifier = Modifier.padding(AppTheme.dimens.spacingLg),
                ) {
                    Icon(Lucide.CircleAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(40.dp))
                    Text(state.loadError!!, color = colors.red, style = AppTheme.typography.body)
                }
            }
            state.dispute != null -> ResolveDisputeContent(
                dispute = state.dispute!!,
                selectedResolution = state.selectedResolution,
                note = state.note,
                submitError = state.submitError,
                isSubmitting = state.isSubmitting,
                canConfirm = state.canConfirm,
                onSelectResolution = component::onSelectResolution,
                onNoteChange = component::onNoteChange,
                onConfirm = component::onConfirm,
            )
        }
    }
}

@Composable
private fun ResolveDisputeContent(
    dispute: AdminDisputeItem,
    selectedResolution: String?,
    note: String,
    submitError: String?,
    isSubmitting: Boolean,
    canConfirm: Boolean,
    onSelectResolution: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirm: () -> Unit,
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
        // Doctor + Patient summary rows
        PartyRow(label = "Doctor", name = dispute.doctor.fullName, specialty = dispute.specialty.name)
        PartyRow(label = "Paciente", name = dispute.patient.fullName, specialty = dispute.patient.email)

        // Dispute reason / evidence
        dispute.disputeReason?.let { reason ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius))
                    .background(colors.redBg)
                    .border(1.dp, colors.border, RoundedCornerShape(dimens.radius))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                Text("Punto crítico", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Text(reason, fontSize = 12.sp, color = colors.muted)
            }
        }

        // Section label
        DisputeSectionLabel("RESOLUCIÓN")

        // Decision option — Pay doctor (PROVIDER)
        DecisionOptionCard(
            title = "Pagar al doctor",
            subtitle = "Libera S/ ${dispute.price} menos comisión",
            icon = Lucide.HandCoins,
            iconColor = colors.navy,
            bgColor = colors.navyTint,
            isSelected = selectedResolution == "PROVIDER",
            onClick = { onSelectResolution("PROVIDER") },
        )

        // Decision option — Refund patient (PATIENT)
        DecisionOptionCard(
            title = "Reembolsar paciente",
            subtitle = "Devuelve monto total",
            icon = Lucide.RotateCcw,
            iconColor = colors.blue,
            bgColor = colors.blueBg,
            isSelected = selectedResolution == "PATIENT",
            onClick = { onSelectResolution("PATIENT") },
        )

        // Note field
        AppTextField(
            value = note,
            onValueChange = onNoteChange,
            label = "Nota de resolución",
            placeholder = "Mín. 10 caracteres...",
            error = submitError,
            singleLine = false,
        )

        // Submit
        AppButton(
            text = "Confirmar resolución",
            onClick = onConfirm,
            variant = AppButtonVariant.Navy,
            loading = isSubmitting,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(dimens.spacingLg))
    }
}

// ── Resolve No-Show screen ────────────────────────────────────────────────────

@Composable
private fun PartyRow(label: String, name: String, specialty: String) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacing12),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 11.sp, color = colors.muted, fontWeight = FontWeight.Medium)
            Text(name, fontSize = 14.sp, color = colors.text, fontWeight = FontWeight.SemiBold)
        }
        Text(specialty, fontSize = 11.sp, color = colors.muted)
    }
}

@Composable
private fun DecisionOptionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    bgColor: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val borderColor = if (isSelected) colors.navy else colors.border
    val containerBg = if (isSelected) colors.navyTint else colors.surface

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(containerBg)
            .border(1.5.dp, borderColor, RoundedCornerShape(dimens.radiusLarge))
            .clickable(onClick = onClick)
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            Text(subtitle, fontSize = 12.sp, color = colors.muted)
        }
        if (isSelected) {
            Box(
                Modifier.size(18.dp).clip(RoundedCornerShape(9.dp)).background(colors.navy),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Lucide.HandCoins,
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(10.dp),
                )
            }
        } else {
            Box(
                Modifier.size(18.dp).clip(RoundedCornerShape(9.dp))
                    .border(1.5.dp, colors.border, RoundedCornerShape(9.dp)),
            )
        }
    }
}

@Composable
private fun DisputeSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = AppTheme.colors.muted,
    )
}
