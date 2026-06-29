package com.inclinic.app.features.admin.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.FileCheck
import com.composables.icons.lucide.HandCoins
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ShieldAlert
import com.composables.icons.lucide.Timer
import com.inclinic.app.features.admin.presentation.component.AdminDashboardComponent
import com.inclinic.app.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(component: AdminDashboardComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = component::onRefresh,
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        if (state.isLoading && state.appointmentsToday == 0 && state.pendingDoctors == 0) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                AdminHeader(onBell = component::onNavigateToNotifications)

                Column(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.error?.let { err -> ErrorBanner(message = err, onRetry = component::onRefresh) }

                    HeroCard(appointmentsToday = state.appointmentsToday)

                    // 2x2 KPI grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricTile(
                                icon = Lucide.Calendar,
                                iconTint = colors.blue,
                                label = "Citas",
                                value = state.appointmentsToday.toString(),
                                trend = "${state.pendingDoctors} doctores pend.",
                                trendColor = colors.muted,
                                modifier = Modifier.weight(1f),
                            )
                            MetricTile(
                                icon = Lucide.HandCoins,
                                iconTint = colors.green,
                                label = "Pagos",
                                value = state.monthRevenue,
                                trend = "este mes",
                                trendColor = colors.green,
                                modifier = Modifier.weight(1f).clickable { component.onNavigateToFinance() },
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricTile(
                                icon = Lucide.ShieldAlert,
                                iconTint = colors.red,
                                label = "Riesgo",
                                value = state.riskCount.toString(),
                                trend = "${state.pendingDisputes} disputas",
                                trendColor = if (state.riskCount > 0) colors.red else colors.muted,
                                modifier = Modifier.weight(1f),
                            )
                            MetricTile(
                                icon = Lucide.Timer,
                                iconTint = colors.amber,
                                label = "SLA",
                                value = "${state.slaPct}%",
                                trend = "dentro de meta",
                                trendColor = colors.muted,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Text(
                        "PRÓXIMAS ACCIONES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = colors.muted,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    if (state.pendingDoctors > 0) {
                        QueueItem(
                            initials = "DR",
                            avatarColor = colors.lav,
                            title = "${state.pendingDoctors} doctor(es) por revisar",
                            subtitle = "Verificación pendiente",
                            badge = null,
                            badgeColor = colors.amber,
                            badgeBg = colors.amberBg,
                            metaIcon = Lucide.FileCheck,
                            metaIconTint = colors.green,
                            metaText = "Aprobar o rechazar solicitudes",
                            actionLabel = "Revisar",
                            onAction = component::onNavigateToDoctorApprovals,
                        )
                    }
                    if (state.pendingDisputes > 0) {
                        QueueItem(
                            initials = "!",
                            avatarColor = colors.red,
                            title = "${state.pendingDisputes} disputa(s) abiertas",
                            subtitle = "Pago retenido · revisión requerida",
                            badge = "Urgente",
                            badgeColor = colors.red,
                            badgeBg = colors.redBg,
                            metaIcon = Lucide.ShieldAlert,
                            metaIconTint = colors.red,
                            metaText = "Fondos en custodia hasta resolver",
                            actionLabel = "Resolver",
                            onAction = component::onNavigateToDisputes,
                        )
                    }
                    if (state.pendingDoctors == 0 && state.pendingDisputes == 0) {
                        Text(
                            "Sin acciones pendientes por ahora.",
                            fontSize = 13.sp,
                            color = colors.muted,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHeader(onBell: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Admin",
            style = AppTheme.typography.displayXSmall,
            fontSize = 22.sp,
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.navy)
                .clickable(onClick = onBell),
        ) {
            Icon(Lucide.Bell, contentDescription = "Notificaciones", tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.redBg)
            .border(1.dp, colors.red.copy(alpha = 0.25f), RoundedCornerShape(AppTheme.dimens.radius))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Lucide.ShieldAlert, contentDescription = null, tint = colors.red, modifier = Modifier.size(18.dp))
        Text(message, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.red, modifier = Modifier.weight(1f))
        Box(
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.red)
                .clickable(onClick = onRetry)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text("Reintentar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HeroCard(appointmentsToday: Int) {
    val colors = AppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.navy)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "OPERACIÓN EN TIEMPO REAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = Color.White.copy(alpha = 0.8f),
        )
        Text(
            "$appointmentsToday citas activas hoy",
            style = AppTheme.typography.displayXSmall,
            fontSize = 24.sp,
            color = Color.White,
        )
        Text(
            "Prioriza aprobaciones, disputas y pagos retenidos desde una sola cola.",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun MetricTile(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    trend: String,
    trendColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(AppTheme.dimens.radius))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.muted)
        }
        Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.text)
        Text(trend, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = trendColor)
    }
}

@Composable
private fun QueueItem(
    initials: String,
    avatarColor: Color,
    title: String,
    subtitle: String,
    badge: String?,
    badgeColor: Color,
    badgeBg: Color,
    metaIcon: ImageVector,
    metaIconTint: Color,
    metaText: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(AppTheme.dimens.radius))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(CircleShape).background(avatarColor),
            ) {
                Text(initials, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.text)
                Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = colors.muted)
            }
            if (badge != null) {
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp)).background(badgeBg).padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(badge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(metaIcon, contentDescription = null, tint = metaIconTint, modifier = Modifier.size(14.dp))
            Text(metaText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colors.muted)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.dimens.radius))
                .background(colors.navy)
                .clickable(onClick = onAction)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(actionLabel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(6.dp))
            Icon(Lucide.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}
