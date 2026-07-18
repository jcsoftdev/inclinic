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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.EllipsisVertical
import com.inclinic.app.features.admin.presentation.component.AdminResolveReportComponent
import com.inclinic.app.features.admin.presentation.component.AdminResolveReportState
import com.inclinic.app.features.admin.presentation.component.ReportDecision
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminResolveReportScreen(
    component: AdminResolveReportComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxSize().background(colors.sand)) {
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    "Reporte #${state.idShort}",
                    style = AppTheme.typography.titleLarge,
                    color = colors.text,
                )
            },
            navigationIcon = { AppBackButton(onClick = component::onBack) },
            actions = {
                var menuOpen by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(
                            Lucide.EllipsisVertical,
                            contentDescription = "Más opciones",
                            tint = colors.navy,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Descartar", color = colors.text) },
                            onClick = {
                                menuOpen = false
                                component.onQuickDismiss()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Escalar", color = colors.red) },
                            onClick = {
                                menuOpen = false
                                component.onEscalate()
                            },
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
        )

        ResolveReportContent(
            state = state,
            onSelectDecision = component::onSelectDecision,
            onNoteChange = component::onAdminNoteChange,
            onConfirm = component::onConfirm,
        )
    }
}

@Composable
private fun ResolveReportContent(
    state: AdminResolveReportState,
    onSelectDecision: (ReportDecision) -> Unit,
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
        // ── Info rows ──────────────────────────────────────────────────────────
        ReportInfoRow(label = "Reportado", value = "${state.reportedUserFullName} · ${state.reportedUserRoleLabel}")
        ReportInfoRow(label = "Categoría", value = state.categoryLabel)

        // ── Motivo del reporte ─────────────────────────────────────────────────
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radiusLarge))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
                .padding(dimens.spacing12),
            verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            Text(
                "Motivo del reporte",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp,
                color = colors.muted,
            )
            Text(
                state.reason,
                fontSize = 14.sp,
                color = colors.text,
                lineHeight = 20.sp,
            )
        }

        // ── Decisión section label ─────────────────────────────────────────────
        Text(
            "DECISIÓN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = colors.muted,
            modifier = Modifier.padding(top = dimens.spacingSm),
        )

        // ── Decision options ───────────────────────────────────────────────────
        ReportDecision.entries.forEach { decision ->
            DecisionCard(
                decision = decision,
                isSelected = state.selectedDecision == decision,
                onClick = { onSelectDecision(decision) },
            )
        }

        // ── Admin note (optional) ──────────────────────────────────────────────
        AppTextField(
            value = state.adminNote,
            onValueChange = onNoteChange,
            label = "Nota del administrador (opcional)",
            placeholder = "Observaciones internas...",
            singleLine = false,
            error = null,
        )

        // ── Submit error ───────────────────────────────────────────────────────
        state.submitError?.let { err ->
            Text(
                text = err,
                color = colors.red,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius))
                    .background(colors.redBg)
                    .padding(dimens.spacing12),
            )
        }

        // ── Resolve button ─────────────────────────────────────────────────────
        AppButton(
            text = "Resolver reporte",
            onClick = onConfirm,
            variant = AppButtonVariant.Navy,
            loading = state.isSubmitting,
            enabled = state.canConfirm,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(dimens.spacingLg))
    }
}

@Composable
private fun ReportInfoRow(label: String, value: String) {
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
        Text(label, fontSize = 12.sp, color = colors.muted, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 14.sp, color = colors.text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DecisionCard(
    decision: ReportDecision,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val borderColor = if (isSelected) colors.navy else colors.border
    val bg = if (isSelected) colors.navyTint else colors.surface

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(bg)
            .border(1.5.dp, borderColor, RoundedCornerShape(dimens.radiusLarge))
            .clickable(onClick = onClick)
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                decision.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
            )
            Text(
                decision.description,
                fontSize = 12.sp,
                color = colors.muted,
            )
        }
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.navy else colors.surface)
                .border(1.5.dp, if (isSelected) colors.navy else colors.border, CircleShape),
        )
    }
}
