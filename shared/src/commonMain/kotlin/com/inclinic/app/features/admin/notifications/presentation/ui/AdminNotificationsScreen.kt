package com.inclinic.app.features.admin.notifications.presentation.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.CalendarPlus
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.FileCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.Stethoscope
import com.composables.icons.lucide.Wallet
import com.inclinic.app.features.admin.notifications.core.model.AdminNotification
import com.inclinic.app.features.admin.notifications.core.model.AdminNotificationKind
import com.inclinic.app.features.admin.notifications.core.port.AdminNotificationFilter
import com.inclinic.app.features.admin.notifications.presentation.component.AdminNotificationsComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.AppBadge
import com.inclinic.app.ui.atoms.AppBadgeTone
import com.inclinic.app.ui.atoms.AppButton
import com.inclinic.app.ui.atoms.AppButtonSize
import com.inclinic.app.ui.atoms.AppButtonVariant
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.molecules.FilterChipRow
import com.inclinic.app.ui.theme.AppTheme
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationsScreen(
    component: AdminNotificationsComponent,
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
                        "Notificaciones",
                        style = AppTheme.typography.displayXSmall,
                        fontSize = 22.sp,
                        color = colors.text,
                    )
                },
                navigationIcon = { AppBackButton(onClick = component::onBack) },
                actions = {
                    Icon(
                        Lucide.CircleCheck,
                        contentDescription = "Marcar todas como leídas",
                        tint = if (state.isActing) colors.muted else colors.navy,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(22.dp)
                            .clickable(enabled = !state.isActing, onClick = component::onMarkAllRead),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.surface),
            )

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
                    FilterChipRow(
                        options = AdminNotificationFilter.entries.map { it.label },
                        selected = state.activeFilter.label,
                        onSelect = { label ->
                            val filter = AdminNotificationFilter.entries.first { it.label == label }
                            component.onFilterChange(filter)
                        },
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

                if (state.filteredNotifications.isEmpty() && !state.isLoading) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(top = dimens.spacingXl),
                            contentAlignment = Alignment.Center,
                        ) {
                            EmptyState(
                                icon = Lucide.Bell,
                                title = "Sin notificaciones",
                                subtitle = "No hay notificaciones que coincidan con el filtro.",
                            )
                        }
                    }
                }

                items(state.filteredNotifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        isActing = state.isActing,
                        onCardClick = { component.onNotificationClick(notification) },
                        onMarkRead = { component.onMarkRead(notification.id) },
                        onDelete = { component.onDelete(notification.id) },
                    )
                }
            }
        }
    }
}

// ── Private sub-composables ───────────────────────────────────────────────────

@Composable
private fun NotificationCard(
    notification: AdminNotification,
    isActing: Boolean,
    onCardClick: () -> Unit,
    onMarkRead: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val (icon, iconFg, iconBg) = kindVisual(notification.kind)

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(colors.surface)
            .border(1.dp, if (notification.isRead) colors.border else colors.navy.copy(alpha = 0.25f), RoundedCornerShape(dimens.radiusLarge))
            // Tapping anywhere on the card marks it read and deep-links by kind (see
            // AdminNotificationsComponent.onNotificationClick). Unmapped kinds / links just
            // mark read and stay here — never crash. The explicit "Marcar leída" / "Eliminar"
            // buttons below still work independently (Button consumes its own click).
            .clickable(onClick = onCardClick)
            .padding(dimens.spacing12),
        verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        ) {
            // Leading colored icon avatar by type
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconBg),
            ) {
                Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(20.dp))
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.text,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    // Relative time label
                    Text(
                        text = notification.createdAt.toRelativeLabel(),
                        fontSize = 11.sp,
                        color = colors.muted,
                    )
                }

                // Body text
                if (notification.body.isNotBlank()) {
                    Text(
                        text = notification.body,
                        fontSize = 12.sp,
                        color = colors.muted,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Status badges row
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!notification.isRead) {
                AppBadge(text = "Nuevo", tone = AppBadgeTone.Info)
            } else {
                AppBadge(text = "Leída", tone = AppBadgeTone.Neutral)
            }
        }

        Spacer(Modifier.height(2.dp))

        // Action row: primary action + delete
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (!notification.isRead) {
                AppButton(
                    text = "Marcar leída",
                    onClick = onMarkRead,
                    variant = AppButtonVariant.Navy,
                    size = AppButtonSize.Sm,
                    modifier = Modifier.weight(1f),
                )
            } else {
                Spacer(Modifier.weight(1f))
            }

            AppButton(
                text = "Eliminar",
                onClick = onDelete,
                variant = AppButtonVariant.Danger,
                size = AppButtonSize.Sm,
                loading = isActing,
            )
        }
    }
}

// ── Visual helpers ────────────────────────────────────────────────────────────

@Composable
private fun kindVisual(kind: AdminNotificationKind): Triple<ImageVector, Color, Color> {
    val colors = AppTheme.colors
    return when (kind) {
        AdminNotificationKind.APPOINTMENT -> Triple(Lucide.CalendarPlus, colors.green, colors.successBg)
        AdminNotificationKind.PAYMENT -> Triple(Lucide.Wallet, colors.info, colors.infoBg)
        AdminNotificationKind.DOCTOR -> Triple(Lucide.Stethoscope, colors.purple, colors.purpleBg)
        AdminNotificationKind.SPECIALTY -> Triple(Lucide.FileCheck, colors.amber, colors.amberBg)
        AdminNotificationKind.MESSAGE -> Triple(Lucide.MessageCircle, colors.navy, colors.navyTint)
        AdminNotificationKind.SYSTEM -> Triple(Lucide.Bell, colors.muted, colors.lav50)
    }
}

/**
 * Produces a relative label from an [Instant].
 * Mirrors the logic from [computeReviewAgeLabel] used elsewhere in the admin module.
 */
private fun Instant.toRelativeLabel(): String = try {
    val iso = toString()
    val datePart = iso.substring(0, 10)
    val parts = datePart.split("-")
    val year = parts[0].toInt(); val month = parts[1].toInt(); val day = parts[2].toInt()
    val approxDayOfYear = (year - 2024) * 365 + (month - 1) * 30 + day
    val nowApprox = (2026 - 2024) * 365 + (6 - 1) * 30 + 1
    val diffDays = nowApprox - approxDayOfYear
    when {
        diffDays <= 0 -> "Hoy"
        diffDays == 1 -> "Ayer"
        diffDays < 30 -> "${diffDays}d"
        else -> "${diffDays / 30}m"
    }
} catch (_: Exception) {
    "—"
}
