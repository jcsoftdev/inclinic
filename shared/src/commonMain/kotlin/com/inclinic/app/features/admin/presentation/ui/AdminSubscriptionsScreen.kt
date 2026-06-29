package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.IconButton
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
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.inclinic.app.features.admin.infrastructure.remote.AdminSubscriptionItem
import com.inclinic.app.features.admin.infrastructure.remote.SubscriptionStatus
import com.inclinic.app.features.admin.presentation.component.AdminSubscriptionsComponent
import com.inclinic.app.features.admin.presentation.component.AdminSubscriptionsFilter
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.molecules.FilterChipRow
import com.inclinic.app.ui.molecules.KpiCard
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSubscriptionsScreen(
    component: AdminSubscriptionsComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    // Manage-subscription dialog
    state.pendingManageItem?.let { item ->
        ManageSubscriptionDialog(
            item = item,
            isActioning = state.isActioning,
            actionError = state.actionError,
            onConfirmDowngrade = {
                component.onConfirmManage(item, newTier = "FREE", expiresAt = null)
            },
            onDismiss = component::onDismissDialog,
        )
    }

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
                        "Suscripciones",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            Lucide.Info,
                            contentDescription = "Información de planes",
                            tint = colors.navy,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )

            if (state.isLoading && state.allItems.isEmpty()) {
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
                // ── Hero stats row ────────────────────────────────────────────
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(dimens.spacingMd),
                    ) {
                        KpiCard(
                            label = "Planes activos",
                            value = state.stats.activeCount.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        KpiCard(
                            label = "Vencen pronto",
                            value = state.stats.expiringSoonCount.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        KpiCard(
                            label = "Pausados",
                            value = state.stats.expiredCount.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // ── Chip filters ──────────────────────────────────────────────
                item {
                    FilterChipRow(
                        options = AdminSubscriptionsFilter.entries.map { it.label },
                        selected = state.activeFilter.label,
                        onSelect = { label ->
                            val filter = AdminSubscriptionsFilter.entries.first { it.label == label }
                            component.onFilterChange(filter)
                        },
                    )
                }

                // ── Error banner ──────────────────────────────────────────────
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

                // ── Empty state ───────────────────────────────────────────────
                if (state.visibleItems.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = dimens.spacingXl),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Lucide.Crown,
                                title = "Sin suscripciones",
                                subtitle = "No hay doctores con plan Premium en este filtro.",
                            )
                        }
                    }
                }

                // ── Subscription items ────────────────────────────────────────
                items(state.visibleItems, key = { it.userId }) { item ->
                    SubscriptionItemCard(
                        item = item,
                        onManage = { component.onManageClicked(item) },
                    )
                }
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun SubscriptionItemCard(
    item: AdminSubscriptionItem,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(dimens.radiusLarge))
            .padding(dimens.spacing12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        // Initials avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.navyTint),
        ) {
            Text(
                text = item.initials,
                color = colors.navy,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            // Name + tier badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimens.spacingXs),
            ) {
                Text(
                    item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                    modifier = Modifier.weight(1f, fill = false),
                )
                SubscriptionStatusBadge(item.status)
            }
            // Tier
            Text(
                if (item.tier == "PREMIUM") "Premium · S/. 99/mes" else "Free",
                fontSize = 11.sp,
                color = colors.muted,
            )
            // Expiry
            Text(
                item.expiryLabel,
                fontSize = 11.sp,
                color = when (item.status) {
                    SubscriptionStatus.EXPIRING -> colors.amber
                    SubscriptionStatus.EXPIRED  -> colors.red
                    else                        -> colors.light
                },
            )
        }

        AppButton(
            text = "Gestionar",
            onClick = onManage,
            size = AppButtonSize.Sm,
            variant = AppButtonVariant.Outline,
        )
    }
}

@Composable
private fun SubscriptionStatusBadge(status: SubscriptionStatus) {
    when (status) {
        SubscriptionStatus.ACTIVE   -> AppBadge(text = "Activa", tone = AppBadgeTone.Success)
        SubscriptionStatus.EXPIRING -> AppBadge(text = "Vence pronto", tone = AppBadgeTone.Warning)
        SubscriptionStatus.EXPIRED  -> AppBadge(text = "Pausada", tone = AppBadgeTone.Error)
    }
}

/**
 * Confirm dialog for managing a subscription.
 *
 * For now, the only action offered is downgrade (PREMIUM → FREE).
 * Extending to "Renovar / Extender" would require a date picker — scope for a future increment.
 */
@Composable
private fun ManageSubscriptionDialog(
    item: AdminSubscriptionItem,
    isActioning: Boolean,
    actionError: String?,
    onConfirmDowngrade: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isActioning) onDismiss() },
        title = { Text("Gestionar suscripción") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${item.name} — plan actual: ${if (item.tier == "PREMIUM") "Premium" else "Free"}",
                    fontSize = 14.sp,
                )
                if (item.tier == "PREMIUM") {
                    Text(
                        "¿Deseas revertir este doctor al plan Free? Se perderán los beneficios Premium al confirmar.",
                        fontSize = 13.sp,
                    )
                } else {
                    Text(
                        "Este doctor ya está en plan Free. Para asignar Premium usa el endpoint de suscripción directamente.",
                        fontSize = 13.sp,
                    )
                }
                actionError?.let { err ->
                    Text(
                        text = err,
                        fontSize = 12.sp,
                        color = AppTheme.colors.red,
                    )
                }
            }
        },
        confirmButton = {
            if (item.tier == "PREMIUM") {
                TextButton(
                    onClick = onConfirmDowngrade,
                    enabled = !isActioning,
                ) {
                    if (isActioning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AppTheme.colors.navy,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text("Revertir a Free", color = AppTheme.colors.red)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isActioning) {
                Text("Cancelar")
            }
        },
    )
}
