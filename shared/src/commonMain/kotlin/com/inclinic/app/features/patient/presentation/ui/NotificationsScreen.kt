package com.inclinic.app.features.patient.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.BellRing
import com.composables.icons.lucide.CalendarCheck
import com.composables.icons.lucide.CreditCard
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.Lucide
import com.inclinic.app.core.model.NotificationType
import com.inclinic.app.features.patient.presentation.component.NotificationFilter
import com.inclinic.app.features.patient.presentation.component.NotificationsComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.SkeletonNotificationRow
import com.inclinic.app.ui.molecules.NotificationRow
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(component: NotificationsComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notificaciones") },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Lucide.ArrowLeft, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = component::onMarkAllRead) {
                        Text("Marcar todas", color = colors.navy, fontSize = 12.sp)
                    }
                },
                windowInsets = WindowInsets(0),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.sand)
                .padding(padding),
        ) {
            state.error?.let { ErrorBanner(message = it, onDismiss = { }) }

            // Filter tabs
            NotificationFilterTabs(
                selected = state.filter,
                onSelected = component::onFilterChange,
            )

            when {
                state.isLoading -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // Three skeleton rows matching NotificationRow height
                    repeat(4) { SkeletonNotificationRow() }
                }
                state.filteredNotifications.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes notificaciones", color = colors.muted, fontSize = 14.sp)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.filteredNotifications, key = { it.id }) { notification ->
                        NotificationRow(
                            title  = notification.title,
                            body   = notification.message,
                            timeAgo = relativeTimeLabel(notification.createdAt),
                            isRead = notification.read,
                            icon   = notificationIcon(notification.type),
                            iconBg = notificationIconBg(notification.type, colors),
                            onClick = { component.onNotificationClick(notification) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationFilterTabs(
    selected: NotificationFilter,
    onSelected: (NotificationFilter) -> Unit,
) {
    val colors = AppTheme.colors
    val tabs = NotificationFilter.entries.toList()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(36.dp))
                    .background(if (isSelected) colors.navy else colors.elevated)
                    .then(
                        if (!isSelected) Modifier.border(1.dp, colors.border, RoundedCornerShape(36.dp))
                        else Modifier,
                    )
                    .clickable { onSelected(tab) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    color = if (isSelected) Color.White else colors.muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

private fun notificationIcon(type: NotificationType): ImageVector = when (type) {
    NotificationType.APPOINTMENT    -> Lucide.CalendarCheck
    NotificationType.PAYMENT        -> Lucide.CreditCard
    NotificationType.MEDICAL_HISTORY -> Lucide.FileText
    NotificationType.SYSTEM         -> Lucide.BellRing
}

/**
 * Per-type icon background color — parallel to [notificationIcon].
 *
 * Maps each [NotificationType] to a semantic surface token:
 * - APPOINTMENT  → greenBg  (health / confirmed)
 * - PAYMENT      → blueBg   (financial)
 * - MEDICAL_HISTORY → purpleBg (records)
 * - SYSTEM       → lav50    (neutral; matches previous default)
 */
private fun notificationIconBg(type: NotificationType, colors: AppColors): Color = when (type) {
    NotificationType.APPOINTMENT    -> colors.greenBg
    NotificationType.PAYMENT        -> colors.blueBg
    NotificationType.MEDICAL_HISTORY -> colors.purpleBg
    NotificationType.SYSTEM         -> colors.lav50
}

private val NotificationFilter.label: String
    get() = when (this) {
        NotificationFilter.ALL          -> "Todas"
        NotificationFilter.APPOINTMENTS -> "Citas"
        NotificationFilter.PAYMENTS     -> "Pagos"
        NotificationFilter.HISTORY      -> "Historia"
    }
