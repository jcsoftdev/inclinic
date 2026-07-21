package com.inclinic.app.features.doctor.notifications.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.inclinic.app.ui.theme.AppColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.CalendarPlus
import com.composables.icons.lucide.FileCheck
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Wallet
import com.inclinic.app.features.doctor.notifications.core.model.DoctorNotification
import com.inclinic.app.features.doctor.notifications.core.model.NotificationKind
import com.inclinic.app.features.doctor.notifications.core.port.NotificationFilter
import com.inclinic.app.features.doctor.notifications.presentation.component.DoctorNotificationsComponent
import com.inclinic.app.ui.theme.AppTheme

@Composable
fun DoctorNotificationsScreen(
    component: DoctorNotificationsComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing20)
                .height(52.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(dimens.radiusXl))
                    .background(AppTheme.colors.surface)
                    .clickable(onClick = component::onBack),
            ) {
                Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text, modifier = Modifier.size(18.dp))
            }
            Text(
                text = "Notificaciones",
                style = typography.displayNano,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Marcar todas",
                style = typography.link.copy(fontSize = 12.sp),
                color = colors.navy,
                modifier = Modifier.clickable(onClick = component::onMarkAllRead),
            )
        }

        // Filter pills
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.spacing20, vertical = dimens.spacingSm),
        ) {
            FilterPill("Todas", state.activeFilter == NotificationFilter.ALL) { component.onFilterChange(NotificationFilter.ALL) }
            FilterPill("Citas", state.activeFilter == NotificationFilter.APPOINTMENTS) { component.onFilterChange(NotificationFilter.APPOINTMENTS) }
            FilterPill("Pagos", state.activeFilter == NotificationFilter.PAYMENTS) { component.onFilterChange(NotificationFilter.PAYMENTS) }
            FilterPill("Compartir", state.activeFilter == NotificationFilter.SHARE) { component.onFilterChange(NotificationFilter.SHARE) }
        }

        state.error?.let {
            Text(it, color = colors.red, style = typography.subtitle, modifier = Modifier.padding(horizontal = dimens.spacingMd))
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = dimens.spacingMd),
                verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
            ) {
                items(state.filteredNotifications, key = { it.id }) { notification ->
                    NotificationCard(notification = notification, onClick = { component.onNotificationClick(notification) })
                }
            }
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Text(
        text = label,
        color = if (selected) Color.White else colors.muted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(AppTheme.dimens.radiusPill))
            .background(if (selected) colors.navy else AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun NotificationCard(
    notification: DoctorNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens
    val typography = AppTheme.typography
    val (icon, iconFg, iconBg) = kindVisual(notification.kind, colors)

    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(dimens.spacing12),
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(dimens.radiusLarge), ambientColor = Color(0x0A000000), spotColor = Color(0x0A000000))
            .clip(RoundedCornerShape(dimens.radiusLarge))
            .background(if (notification.isRead) AppTheme.colors.surface.copy(alpha = 0.8f) else AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
        ) {
            Icon(icon, contentDescription = null, tint = iconFg, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    notification.title,
                    style = typography.subtitle,
                    fontWeight = FontWeight.Bold,
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                )
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(dimens.spacingXs))
                            .background(colors.navy),
                    )
                }
            }
            Text(
                notification.body,
                style = typography.subtitle.copy(fontSize = 12.sp),
                color = colors.muted,
            )
        }
    }
}

private fun kindVisual(kind: NotificationKind, colors: AppColors): Triple<ImageVector, Color, Color> = when (kind) {
    NotificationKind.APPOINTMENT -> Triple(Lucide.CalendarPlus, colors.green, colors.successBg)
    NotificationKind.PAYMENT -> Triple(Lucide.Wallet, colors.info, colors.infoBg)
    NotificationKind.REVIEW -> Triple(Lucide.Star, colors.amber, colors.amberBg)
    NotificationKind.MESSAGE -> Triple(Lucide.MessageCircle, colors.navy, colors.navyTint)
    NotificationKind.SHARE -> Triple(Lucide.FileCheck, colors.purple, colors.purpleBg)
    NotificationKind.SYSTEM -> Triple(Lucide.Bell, colors.muted, colors.lav50)
}
