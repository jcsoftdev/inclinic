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
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.HandCoins
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.TriangleAlert
import com.inclinic.app.core.util.DetailLoadState
import com.inclinic.app.features.admin.infrastructure.remote.AdminNoShowItem
import com.inclinic.app.features.admin.presentation.component.AdminResolveNoShowComponent
import com.inclinic.app.features.admin.presentation.component.toDetailLoadState
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.DetailErrorState
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminResolveNoShowScreen(
    component: AdminResolveNoShowComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    "No-Show #${state.noShow?.id?.takeLast(4) ?: "…"}",
                    style = AppTheme.typography.titleLarge,
                    color = colors.text,
                )
            },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
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
            is DetailLoadState.Content -> ResolveNoShowContent(
                noShow = loadState.value,
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
private fun ResolveNoShowContent(
    noShow: AdminNoShowItem,
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
        // Summary brief
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius))
                .background(colors.elevated)
                .border(1.dp, colors.border, RoundedCornerShape(dimens.radius))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Text(
                "Paciente no ingresó a la sala",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
            )
            Text(
                "${noShow.doctor.fullName} · ${noShow.specialty.name} · S/ ${noShow.price} retenido",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.muted,
            )
        }

        // Evidence banner
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius))
                .background(colors.amberBg)
                .border(1.dp, colors.amber, RoundedCornerShape(dimens.radius))
                .padding(dimens.spacing12),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Icon(Lucide.TriangleAlert, contentDescription = null, tint = colors.amber, modifier = Modifier.size(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Evidencia del doctor", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
                Text("Esperó 12 minutos y envió mensajes dentro de la sala.", fontSize = 12.sp, color = colors.muted)
            }
        }

        // Section label
        NSectionLabel("DECISIÓN")

        // Release to doctor
        NoShowDecisionCard(
            title = "Liberar al doctor",
            subtitle = "Aplica regla no-show",
            icon = Lucide.HandCoins,
            iconColor = colors.navy,
            bgColor = colors.navyTint,
            isSelected = selectedResolution == "RELEASE_TO_DOCTOR",
            onClick = { onSelectResolution("RELEASE_TO_DOCTOR") },
        )

        // Refund to patient
        NoShowDecisionCard(
            title = "Reembolsar",
            subtitle = "Marca excepción al paciente",
            icon = Lucide.RotateCcw,
            iconColor = colors.blue,
            bgColor = colors.blueBg,
            isSelected = selectedResolution == "REFUND_TO_PATIENT",
            onClick = { onSelectResolution("REFUND_TO_PATIENT") },
        )

        // Note field
        AppTextField(
            value = note,
            onValueChange = onNoteChange,
            label = "Nota de decisión",
            placeholder = "Mín. 10 caracteres...",
            error = submitError,
            singleLine = false,
        )

        // Submit
        AppButton(
            text = "Continuar",
            onClick = onConfirm,
            variant = AppButtonVariant.Navy,
            loading = isSubmitting,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(dimens.spacingLg))
    }
}

@Composable
private fun NoShowDecisionCard(
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
            Icon(
                Lucide.CircleCheck,
                contentDescription = null,
                tint = colors.navy,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Box(
                Modifier.size(20.dp).clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, colors.border, RoundedCornerShape(10.dp)),
            )
        }
    }
}

@Composable
private fun NSectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = AppTheme.colors.muted,
    )
}
