package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Circle
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.CircleX
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
import com.inclinic.app.core.model.PackageSession
import com.inclinic.app.core.model.PackageStatus
import com.inclinic.app.core.model.SessionStatus
import com.inclinic.app.core.model.TherapyPackage
import com.inclinic.app.features.patient.presentation.component.SessionsTab
import com.inclinic.app.features.patient.presentation.component.TherapyPackageDetailComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppColors
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapyPackageDetailScreen(
    component: TherapyPackageDetailComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val typography = AppTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Top bar
        TopAppBar(
            windowInsets = WindowInsets(0),
            title = {
                Text(
                    text = "Detalle Paquete",
                    style = typography.body.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = colors.text,
                )
            },
            navigationIcon = {
                IconButton(onClick = component::onBack) {
                    Icon(Lucide.ArrowLeft, contentDescription = "Volver", tint = colors.text)
                }
            },
            actions = {
                IconButton(onClick = { /* Share */ }) {
                    Icon(Lucide.Share2, contentDescription = "Compartir", tint = colors.navy)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.sand),
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
            state.error != null -> {
                ErrorBanner(message = state.error!!, onDismiss = component::onErrorDismissed)
            }
            state.therapyPackage != null -> {
                val pkg = state.therapyPackage!!
                val sessions = state.sessions

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Progress card
                    item {
                        ProgressCard(pkg = pkg)
                    }

                    // Sessions tabs
                    item {
                        SessionsTabs(
                            selected = state.selectedTab,
                            onSelected = component::onTabChange,
                        )
                    }

                    // Filtered sessions
                    val filtered = when (state.selectedTab) {
                        SessionsTab.UPCOMING -> sessions.filter {
                            it.status == SessionStatus.SCHEDULED || it.status == SessionStatus.UNSCHEDULED
                        }
                        SessionsTab.HISTORY -> sessions.filter {
                            it.status == SessionStatus.COMPLETED || it.status == SessionStatus.CANCELLED
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No hay sesiones", color = colors.muted, fontSize = 14.sp)
                            }
                        }
                    } else {
                        items(filtered, key = { "${it.sessionNumber}-${it.id}" }) { session ->
                            SessionRow(session = session)
                        }
                    }

                    item { Spacer(Modifier.height(80.dp)) }
                }

                // CTA buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.sand)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.navy)
                            .clickable { component.onScheduleNextSession() }
                            .padding(vertical = 14.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Lucide.Calendar, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Agendar siguiente sesión",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    // Estado de cuenta: saldo, abonos y el coste de seguir fraccionando.
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.surface)
                            .clickable { component.onViewStatement() }
                            .padding(vertical = 14.dp),
                    ) {
                        Text(
                            text = "Ver estado de cuenta y pagos",
                            color = colors.navy,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

// ── Progress Card ────────────────────────────────────────────────────────────

@Composable
private fun ProgressCard(pkg: TherapyPackage) {
    val colors = AppTheme.colors
    val progress = if (pkg.totalSessions > 0) pkg.completedSessions.toFloat() / pkg.totalSessions else 0f

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.navy)
            .padding(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = pkg.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                if (pkg.doctorName != null) {
                    Text(
                        text = pkg.doctorName,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                    )
                }
                if (pkg.specialtyName != null) {
                    Text(
                        text = pkg.specialtyName,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                    )
                }
            }
            val statusUi = detailPackageStatusUi(pkg.status, colors)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusUi.background)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = statusUi.label,
                    color = statusUi.foreground,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Progress
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${pkg.completedSessions}/${pkg.totalSessions} sesiones",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f),
            strokeCap = StrokeCap.Round,
        )

        // Price row
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "S/. ${pkg.pricePerSession}/sesión",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
            )
            Text(
                text = "Total: S/. ${pkg.totalPrice}",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        // Meta stats
        val remaining = (pkg.totalSessions - pkg.completedSessions).coerceAtLeast(0)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MetaStat(value = pkg.completedSessions.toString(), label = "Usadas")
            MetaStat(value = remaining.toString(), label = "Restantes")
            MetaStat(value = pkg.totalSessions.toString(), label = "Total")
        }
    }
}

@Composable
private fun MetaStat(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
        )
    }
}

// ── Package status chip ───────────────────────────────────────────────────────

private data class DetailPackageStatusUi(val label: String, val background: Color, val foreground: Color)

private fun detailPackageStatusUi(status: PackageStatus, colors: AppColors): DetailPackageStatusUi = when (status) {
    PackageStatus.ACTIVE -> DetailPackageStatusUi("ACTIVO", colors.successBg, colors.green)
    PackageStatus.PENDING_PAYMENT -> DetailPackageStatusUi("PENDIENTE", colors.amberBg, colors.amber)
    PackageStatus.COMPLETED -> DetailPackageStatusUi("COMPLETADO", colors.navyTint, colors.navy)
    PackageStatus.CANCELLED -> DetailPackageStatusUi("CANCELADO", colors.errorBg, colors.error)
    PackageStatus.EXPIRED -> DetailPackageStatusUi("EXPIRADO", colors.base, colors.muted)
}

// ── Sessions Tabs ────────────────────────────────────────────────────────────

@Composable
private fun SessionsTabs(
    selected: SessionsTab,
    onSelected: (SessionsTab) -> Unit,
) {
    val colors = AppTheme.colors
    val tabs = SessionsTab.entries.toList()
    val outerShape = RoundedCornerShape(20.dp)
    val tabShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, outerShape, ambientColor = Color(0x0F000000), spotColor = Color(0x0F000000))
            .clip(outerShape)
            .background(AppTheme.colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.55f), outerShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(tabShape)
                    .background(if (isSelected) colors.navy else Color.Transparent)
                    .clickable { onSelected(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    color = if (isSelected) Color.White else colors.muted,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                )
            }
        }
    }
}

private val SessionsTab.label: String
    get() = when (this) {
        SessionsTab.UPCOMING -> "Próximas"
        SessionsTab.HISTORY -> "Historial"
    }

// ── Session Row ──────────────────────────────────────────────────────────────

@Composable
private fun SessionRow(session: PackageSession) {
    val colors = AppTheme.colors
    val typography = AppTheme.typography
    val statusIcon = when (session.status) {
        SessionStatus.COMPLETED -> Lucide.CircleCheck
        SessionStatus.CANCELLED -> Lucide.CircleX
        SessionStatus.SCHEDULED -> Lucide.Clock
        SessionStatus.UNSCHEDULED -> Lucide.Circle
    }
    val statusColor = when (session.status) {
        SessionStatus.COMPLETED -> colors.green
        SessionStatus.CANCELLED -> colors.red
        SessionStatus.SCHEDULED -> colors.amber
        SessionStatus.UNSCHEDULED -> colors.muted
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surface)
            .padding(14.dp),
    ) {
        // Status icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.1f)),
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Sesión ${session.sessionNumber}",
                style = typography.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                color = colors.text,
            )
            val subtitle = when {
                session.scheduledAt != null -> {
                    val local = session.scheduledAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    "${local.day}/${local.month.number}/${local.year} - ${local.hour}:${local.minute.toString().padStart(2, '0')}"
                }
                session.status == SessionStatus.UNSCHEDULED -> "Sin agendar"
                else -> session.status.name.lowercase().replaceFirstChar { it.uppercase() }
            }
            Text(
                text = subtitle,
                style = typography.body.copy(fontSize = 11.sp),
                color = colors.muted,
            )
        }

        // Visit type badge
        session.visitType?.let { vt ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.navyTint)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = vt.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = colors.navy,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
