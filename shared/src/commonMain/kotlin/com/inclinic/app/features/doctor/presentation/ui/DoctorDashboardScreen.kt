package com.inclinic.app.features.doctor.presentation.ui

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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.CircleCheck
import com.composables.icons.lucide.ClipboardPlus
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Package
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.TrendingUp
import com.composables.icons.lucide.UserPlus
import com.composables.icons.lucide.Users
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.doctor.presentation.component.DoctorDashboardComponent
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDashboardScreen(component: DoctorDashboardComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = component::onRefresh,
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand)
            .testTag("doctor_dashboard"),
    ) {
        if (state.isLoading && state.todayCount == 0 && state.upcomingAppointments.isEmpty()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                DashboardHeader(onBell = component::onNavigateToNotifications)

                Column(
                    Modifier.fillMaxWidth().padding(horizontal = dimens.spacingMd, vertical = dimens.spacing12),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    state.error?.let { err -> Text(err, style = AppTheme.typography.subtitle, color = colors.red) }

                    HeroCard(
                        todayCount = state.todayCount,
                        monthlyEarnings = state.monthlyEarnings,
                        completedTodayPct = state.completedTodayPct,
                        onClick = component::onNavigateToSchedule,
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KpiTile(
                            icon = Lucide.Star,
                            iconTint = colors.amber,
                            label = "RATING",
                            value = state.ratingAverage.formatDecimal(1),
                            caption = "${state.ratingCount} reseñas",
                            captionColor = colors.muted,
                            modifier = Modifier.weight(1f),
                        )
                        KpiTile(
                            icon = Lucide.CircleCheck,
                            iconTint = colors.green,
                            label = "COMPLETADAS",
                            value = state.completedCount.toString(),
                            caption = "+${state.completedThisMonth} este mes",
                            captionColor = colors.green,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = component::onNavigateToPendingAppointments),
                        )
                        KpiTile(
                            icon = Lucide.Users,
                            iconTint = colors.purple,
                            label = "PACIENTES",
                            value = state.patientsCount.toString(),
                            caption = "recurrentes",
                            captionColor = colors.muted,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(onClick = component::onNavigateToPatients),
                        )
                    }
                }

                Column(
                    Modifier.fillMaxWidth().padding(horizontal = dimens.spacingMd),
                    verticalArrangement = Arrangement.spacedBy(dimens.spacingSm),
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Próximas citas", style = AppTheme.typography.titleLarge, color = colors.text)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Ver todas",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.navy,
                            modifier = Modifier.clickable(onClick = component::onNavigateToSchedule),
                        )
                    }

                    if (state.upcomingAppointments.isEmpty()) {
                        Text("Sin próximas citas", fontSize = 13.sp, color = colors.muted)
                    } else {
                        state.upcomingAppointments.forEach { appt ->
                            UpcomingAppointmentCard(
                                appointment = appt,
                                onClick = { component.onAppointmentTap(appt.id) },
                            )
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    QuickAction(Lucide.ClipboardPlus, colors.navy, "Nueva ficha", Modifier.weight(1f), component::onCreateMedicalRecord)
                    QuickAction(Lucide.Package, colors.purple, "Paquete", Modifier.weight(1f), component::onNavigateToPackages)
                    QuickAction(Lucide.UserPlus, colors.green, "Paciente", Modifier.weight(1f), component::onNavigateToPatients)
                    QuickAction(Lucide.TrendingUp, colors.teal, "Ingresos", Modifier.weight(1f), component::onNavigateToIncome)
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(onBell: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Buenos días,", fontSize = 13.sp, color = colors.muted)
            Text("Dra. Patricia", style = AppTheme.typography.displayXSmall, color = colors.text)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.dp, colors.border, CircleShape)
                .clickable(onClick = onBell),
        ) {
            Icon(Lucide.Bell, contentDescription = "Notificaciones", tint = colors.navy, modifier = Modifier.size(18.dp))
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 11.dp, end = 9.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(colors.red),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.navy),
        ) {
            Text("DP", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HeroCard(todayCount: Int, monthlyEarnings: String, completedTodayPct: Int, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppTheme.dimens.radiusXl))
            .background(colors.navy)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("CITAS DE HOY", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = colors.lavLight)
                Text(todayCount.toString(), style = AppTheme.typography.displayLarge, fontSize = 42.sp, color = Color.White)
            }
            Box(Modifier.size(width = 1.dp, height = 48.dp).background(colors.navyLight))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("INGRESOS MES", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, color = colors.lavLight)
                Text(monthlyEarnings, style = AppTheme.typography.displaySmall, fontSize = 30.sp, color = Color.White)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.13f)),
            ) {
                val fraction = (completedTodayPct.coerceIn(0, 100)) / 100f
                Box(
                    Modifier.fillMaxWidth(fraction).height(6.dp).clip(RoundedCornerShape(3.dp))
                        .background(Color.White),
                )
            }
            Text(
                "Has completado $completedTodayPct% de tus citas de hoy",
                fontSize = 11.sp,
                color = colors.lavLight,
            )
        }
    }
}

@Composable
private fun KpiTile(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    caption: String,
    captionColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(AppTheme.dimens.radiusLarge))
            .background(colors.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(14.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = colors.muted)
        }
        Text(value, style = AppTheme.typography.titleLarge, fontSize = 22.sp, color = colors.text)
        Text(caption, fontSize = 11.sp, color = captionColor)
    }
}

@Composable
private fun QuickAction(icon: ImageVector, iconTint: Color, label: String, modifier: Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(AppTheme.dimens.radiusLarge))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.text)
    }
}

@Composable
private fun UpcomingAppointmentCard(appointment: Appointment, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val time = appointment.startsAt.toLocalDateTime(TimeZone.currentSystemDefault()).time
    val timeLabel = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    val name = appointment.patientId
    val initials = name.split(" ", "-", "_").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "?" }
    val isVirtual = appointment.visitType == VisitType.VIRTUAL

    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(AppTheme.dimens.radiusLarge))
            .background(colors.surface).clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.navyTint),
        ) {
            Text(initials, color = colors.navy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(name, style = AppTheme.typography.body, fontWeight = FontWeight.Bold, color = colors.text)
            Text(
                if (isVirtual) "$timeLabel · Telemedicina" else timeLabel,
                fontSize = 11.sp,
                color = colors.muted,
            )
        }
    }
}
