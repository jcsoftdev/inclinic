package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.CircleHelp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Stethoscope
import com.inclinic.app.features.admin.infrastructure.remote.AdminSpecialtyRequestItem
import com.inclinic.app.features.admin.presentation.component.AdminSpecialtyRequestsComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.AppTextField
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSpecialtyRequestsScreen(
    component: AdminSpecialtyRequestsComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = component::onRefresh,
        modifier = modifier.fillMaxSize().background(colors.sand),
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Text(
                        "Solicitudes",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    Icon(
                        Lucide.CircleHelp,
                        contentDescription = "Información",
                        tint = colors.navy,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(22.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )

            if (state.isLoading && state.items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.navy)
                }
                return@PullToRefreshBox
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = dimens.spacingMd,
                    end = dimens.spacingMd,
                    top = dimens.spacing12,
                    bottom = dimens.spacingLg,
                ),
                verticalArrangement = Arrangement.spacedBy(dimens.spacing12),
            ) {
                item {
                    InfoBanner(
                        title = "Solicitudes de nuevas categorías",
                        description = "Evalúa demanda, documentación y duplicidad antes de crear catálogo.",
                        tone = InfoBannerTone.Info,
                    )
                }

                state.error?.let { err ->
                    item {
                        Text(
                            text = err,
                            color = colors.red,
                            style = AppTheme.typography.body,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(dimens.radius))
                                .background(colors.redBg)
                                .padding(dimens.spacing12),
                        )
                    }
                }

                if (state.items.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = dimens.spacingXl),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Lucide.Stethoscope,
                                title = "Sin solicitudes pendientes",
                                subtitle = "No hay solicitudes de nuevas especialidades.",
                            )
                        }
                    }
                }

                items(state.items, key = { it.id }) { item ->
                    SpecialtyRequestCard(
                        item = item,
                        onEvaluate = { component.onOpenEvaluate(item.id) },
                    )
                }
            }
        }
    }

    // Evaluate sheet — AlertDialog (no bottom-sheet API in KMP without extra dep)
    if (state.showEvaluateSheet) {
        EvaluateRequestDialog(
            item = state.evaluatingItem,
            selectedAction = state.selectedAction,
            reason = state.reason,
            isSubmitting = state.isSubmitting,
            submitError = state.submitError,
            canConfirm = state.canConfirm,
            onSelectAction = component::onSelectAction,
            onReasonChange = component::onReasonChange,
            onConfirm = component::onConfirmEvaluate,
            onDismiss = component::onDismissEvaluate,
        )
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun SpecialtyRequestCard(
    item: AdminSpecialtyRequestItem,
    onEvaluate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            // Initials avatar with color derived from first letter
            val avatarBg = colors.navyTint
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
            ) {
                Text(
                    text = item.doctorInitials,
                    color = colors.navy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    item.specialtyName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
                Text(
                    "Solicitada por ${item.doctorFullName}",
                    fontSize = 12.sp,
                    color = colors.muted,
                )
            }

            // Priority badge — not in response; shown as "Pendiente" (real status)
            AppBadge(
                text = item.status.lowercase().replaceFirstChar { it.uppercase() },
                tone = AppBadgeTone.Warning,
            )
        }

        item.comment?.takeIf { it.isNotBlank() }?.let { comment ->
            Text(
                text = comment,
                fontSize = 12.sp,
                color = colors.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius))
                    .background(colors.sand)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }

        AppButton(
            text = "Evaluar →",
            onClick = onEvaluate,
            variant = AppButtonVariant.Navy,
            size = AppButtonSize.Sm,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun EvaluateRequestDialog(
    item: AdminSpecialtyRequestItem?,
    selectedAction: String?,
    reason: String,
    isSubmitting: Boolean,
    submitError: String?,
    canConfirm: Boolean,
    onSelectAction: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Column {
                Text("Evaluar solicitud", style = AppTheme.typography.titleLarge, color = colors.text)
                if (item != null) {
                    Text(
                        item.specialtyName,
                        fontSize = 13.sp,
                        color = colors.muted,
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(dimens.spacing12)) {
                // Action selection
                ActionOptionRow(
                    label = "Aprobar",
                    description = "Agrega la especialidad al catálogo del doctor.",
                    isSelected = selectedAction == "approve",
                    onClick = { onSelectAction("approve") },
                )
                ActionOptionRow(
                    label = "Rechazar",
                    description = "Acumula un strike al doctor (máx 3).",
                    isSelected = selectedAction == "reject",
                    onClick = { onSelectAction("reject") },
                )

                if (selectedAction == "reject") {
                    AppTextField(
                        value = reason,
                        onValueChange = onReasonChange,
                        label = "Razón de rechazo *",
                        placeholder = "Mín. 10 caracteres",
                        singleLine = false,
                        error = submitError?.takeIf { reason.trim().length < 10 },
                    )
                }

                if (submitError != null && !(selectedAction == "reject" && reason.trim().length < 10)) {
                    Text(
                        text = submitError,
                        color = colors.red,
                        fontSize = 12.sp,
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = if (selectedAction == "approve") "Aprobar" else "Rechazar",
                onClick = onConfirm,
                loading = isSubmitting,
                enabled = canConfirm,
                variant = if (selectedAction == "reject") AppButtonVariant.Danger else AppButtonVariant.Navy,
                size = AppButtonSize.Sm,
            )
        },
        dismissButton = {
            TextButton(onClick = { if (!isSubmitting) onDismiss() }) {
                Text("Cancelar", color = colors.muted)
            }
        },
        containerColor = colors.surface,
    )
}

@Composable
private fun ActionOptionRow(
    label: String,
    description: String,
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
            .clip(RoundedCornerShape(dimens.radius))
            .background(bg)
            .border(1.5.dp, borderColor, RoundedCornerShape(dimens.radius))
            .clickable(onClick = onClick)
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            Text(description, fontSize = 11.sp, color = colors.muted)
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
