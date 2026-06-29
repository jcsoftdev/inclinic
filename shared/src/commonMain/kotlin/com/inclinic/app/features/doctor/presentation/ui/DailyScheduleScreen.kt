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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.AlarmClock
import com.composables.icons.lucide.CalendarPlus
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.doctor.presentation.component.DailyScheduleComponent
import com.inclinic.app.ui.atoms.EmptyState
import com.inclinic.app.ui.atoms.ErrorState
import com.inclinic.app.ui.atoms.SkeletonListRows
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScheduleScreen(component: DailyScheduleComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val dimens = AppTheme.dimens

    Column(Modifier.fillMaxSize().background(colors.sand).then(modifier)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Agenda", style = AppTheme.typography.displaySmall, color = colors.text)
                Text(
                    text = state.date?.let { "$it · ${state.appointments.size} citas" } ?: "${state.appointments.size} citas",
                    style = AppTheme.typography.subtitle,
                    fontSize = 12.sp,
                    color = colors.muted,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.dp, colors.border, CircleShape)
                        .clickable(onClick = component::onOpenRescheduleQueue),
                ) {
                    Icon(Lucide.AlarmClock, contentDescription = "Solicitudes de reagenda", tint = colors.navy, modifier = Modifier.size(18.dp))
                }
                CircleStep(Icons.Default.ChevronLeft, "Día anterior", component::onPreviousDay)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.navy)
                        .clickable(onClick = component::onNextDay),
                ) {
                    Icon(Lucide.CalendarPlus, contentDescription = "Día siguiente", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(dimens.spacingSm),
        ) {
            SegTab("Próximas", selected = true, Modifier.weight(1f))
            SegTab("Por completar", selected = false, Modifier.weight(1f))
            SegTab("Historial", selected = false, Modifier.weight(1f))
        }

        when {
            state.isLoading -> {
                SkeletonListRows(
                    count = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                )
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(
                        modifier    = Modifier.fillMaxWidth(),
                        title       = "No se pudo cargar",
                        subtitle    = "Revisa tu conexion e intentalo de nuevo.",
                        retryLabel  = "Reintentar",
                        onRetry     = component::onNextDay,
                    )
                }
            }
            state.appointments.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title    = "Sin citas para este dia",
                        subtitle = "No hay citas agendadas para este dia.",
                    )
                }
            }
            else -> {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.appointments, key = { it.id }) { appointment ->
                        AgendaCard(
                            appointment = appointment,
                            onClick = { component.onAppointmentTap(appointment.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CircleStep(icon: ImageVector, cd: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(1.dp, colors.border, CircleShape)
            .clickable(onClick = onClick),
    ) {
        Icon(icon, contentDescription = cd, tint = colors.navy, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SegTab(label: String, selected: Boolean, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(AppTheme.dimens.radius))
            .background(if (selected) colors.navy else colors.surface)
            .then(if (selected) Modifier else Modifier.border(1.dp, colors.border, RoundedCornerShape(AppTheme.dimens.radius))),
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else colors.muted,
        )
    }
}

@Composable
private fun AgendaCard(appointment: Appointment, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val time = appointment.startsAt.toLocalDateTime(TimeZone.currentSystemDefault()).time
    val timeLabel = "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
    val durationMin = (appointment.endsAt - appointment.startsAt).inWholeMinutes

    val initials = appointment.patientId
        .split(" ", "-", "_")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifBlank { "?" }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.navyTint),
            ) {
                Text(initials, color = colors.navy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(appointment.patientId, style = AppTheme.typography.body, fontWeight = FontWeight.Bold, color = colors.text)
                appointment.notes?.takeIf { it.isNotBlank() }?.let {
                    Text(it, fontSize = 11.sp, color = colors.muted)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(timeLabel, style = AppTheme.typography.titleLarge, fontSize = 16.sp, color = colors.text)
                Text("$durationMin min", fontSize = 10.sp, color = colors.muted)
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            VisitTypeChip(appointment.visitType)
            StatusChip(appointment.status)
        }
    }
}

@Composable
private fun VisitTypeChip(visitType: VisitType) {
    val colors = AppTheme.colors
    val (icon, tint, bg, label) = when (visitType) {
        VisitType.VIRTUAL -> Quad(Lucide.Video, colors.blue, colors.blueBg, "TELEMEDICINA")
        VisitType.HOME -> Quad(Lucide.House, colors.teal, colors.tealBg, "DOMICILIO")
        VisitType.CLINIC -> Quad(Lucide.Building2, colors.navy, colors.navyTint, "CONSULTORIO")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(11.dp))
        Text(label, color = tint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun StatusChip(status: AppointmentStatus) {
    val colors = AppTheme.colors
    val (label, textColor, bg) = when (status) {
        AppointmentStatus.CONFIRMED -> Triple("CONFIRMADA", colors.green, colors.greenBg)
        AppointmentStatus.IN_PROGRESS -> Triple("EN CAMINO", colors.amber, colors.amberBg)
        AppointmentStatus.SCHEDULED -> Triple("AGENDADA", colors.navy, colors.navyTint)
        AppointmentStatus.COMPLETED -> Triple("COMPLETADA", colors.green, colors.greenBg)
        AppointmentStatus.NO_SHOW -> Triple("NO SHOW", colors.red, colors.redBg)
        AppointmentStatus.CANCELLED_BY_PATIENT, AppointmentStatus.CANCELLED_BY_DOCTOR -> Triple("CANCELADA", colors.red, colors.redBg)
        else -> Triple(status.name, colors.muted, colors.lav50)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(label, color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

private data class Quad(val icon: ImageVector, val tint: Color, val bg: Color, val label: String)
