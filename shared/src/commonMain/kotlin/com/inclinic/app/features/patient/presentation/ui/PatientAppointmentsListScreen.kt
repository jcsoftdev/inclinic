package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CalendarClock
import com.inclinic.app.ui.atoms.EmptyState
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.patient.presentation.component.AppointmentsTab
import com.inclinic.app.features.patient.presentation.component.PatientAppointmentsListComponent
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.atoms.PatientTab
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

@Composable
fun PatientAppointmentsListScreen(component: PatientAppointmentsListComponent, onNavTabSelected: (PatientTab) -> Unit = {}, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val tabs = AppointmentsTab.entries.toList()
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Mis Citas", color = colors.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            AppointmentTabs(
                tabs = tabs,
                selected = state.selectedTab,
                onSelected = component::onTabChange,
            )

            state.error?.let {
                ErrorBanner(message = it, onDismiss = component::onErrorDismissed)
            }

            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.navy)
                    }
                    state.appointments.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            title = "Aún no tienes citas",
                            subtitle = "Cuando agendes una cita aparecerá aquí.",
                            icon = Lucide.Calendar,
                            actionLabel = "Buscar doctores",
                            onAction = component::onSearchDoctors,
                        )
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(state.appointments, key = { it.id }) { appt ->
                            AppointmentCard(
                                appt = appt,
                                onClick = { component.onAppointmentTapped(appt.id) },
                                onPayNow = { component.onPayNow(appt.id) },
                                onCancel = { component.onCancel(appt.id) },
                                onReschedule = { component.onReschedule(appt.id) },
                                onRespondReschedule = { component.onRespondReschedule(appt.id) },
                            )
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun AppointmentTabs(
    tabs: List<AppointmentsTab>,
    selected: AppointmentsTab,
    onSelected: (AppointmentsTab) -> Unit,
) {
    val colors = AppTheme.colors
    val outerShape = RoundedCornerShape(20.dp)
    val tabShape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, outerShape, ambientColor = Color(0x0F000000), spotColor = Color(0x0F000000))
            .clip(outerShape)
            .background(AppTheme.colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.55f), outerShape)
            .horizontalScroll(rememberScrollState())
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .clip(tabShape)
                    .background(if (isSelected) colors.navy else Color.Transparent)
                    .clickable { onSelected(tab) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tab.label,
                    color = if (isSelected) Color.White else colors.muted,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
    }
}

@Composable
private fun AppointmentCard(appt: Appointment, onClick: () -> Unit, onPayNow: () -> Unit, onCancel: () -> Unit, onReschedule: () -> Unit, onRespondReschedule: () -> Unit) {
    val colors = AppTheme.colors
    val now = kotlin.time.Clock.System.now()
    val isPaymentExpired = appt.status == AppointmentStatus.PENDING_PAYMENT &&
        appt.paymentDeadline != null && appt.paymentDeadline <= now
    val statusUi = when {
        isPaymentExpired -> StatusUi("PAGO VENCIDO", colors.redBg, colors.red)
        appt.needsClosure -> StatusUi("POR CONFIRMAR", colors.amberBg, colors.amber)
        else -> statusUi(appt.status)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x15000000), spotColor = Color(0x15000000))
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatusBadge(text = statusUi.label, background = statusUi.background, foreground = statusUi.foreground)
                Text(
                    text = "S/. ${appt.consultationFee.toInt()}",
                    color = colors.text,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = doctorLine(appt),
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = appointmentDateLine(appt),
                color = colors.text,
                fontSize = 13.sp,
            )

            if (appt.hasPendingReschedule) {
                PendingRescheduleActions(onRespond = onRespondReschedule)
            } else {
                when {
                    isPaymentExpired -> ExpiredPaymentNotice()
                    appt.status == AppointmentStatus.PENDING_PAYMENT -> PendingPaymentActions(appt, onPayNow = onPayNow)
                    appt.status == AppointmentStatus.CONFIRMED || appt.status == AppointmentStatus.SCHEDULED ->
                        ConfirmedActions(appt, onCancel = onCancel, onReschedule = onReschedule)
                    else -> Unit
                }
            }
    }
}

@Composable
private fun PendingPaymentActions(appt: Appointment, onPayNow: () -> Unit) {
    val colors = AppTheme.colors
    val remaining = appt.paymentDeadline?.let { remainingLabel(it) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(Lucide.Clock, contentDescription = null, tint = colors.navy, modifier = Modifier.size(13.dp))
        Text(
            text = appt.paymentDeadline?.let { "Paga antes de ${timeLabel(it)}" } ?: "Pago pendiente",
            color = colors.text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (remaining != null) {
            Text(
                text = "· $remaining",
                color = colors.amber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.navy)
            .clickable(onClick = onPayNow)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text("Pagar Ahora", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ExpiredPaymentNotice() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.redBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Lucide.Clock, contentDescription = null, tint = colors.red, modifier = Modifier.size(14.dp))
        Text(
            text = "El plazo de pago venció · la reserva se liberó",
            color = colors.red,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PendingRescheduleActions(onRespond: () -> Unit) {
    val colors = AppTheme.colors
    val purple = AppTheme.colors.purple
    val purpleBg = AppTheme.colors.purpleBg

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(purpleBg)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Lucide.CalendarClock, contentDescription = null, tint = purple, modifier = Modifier.size(14.dp))
            Text("Reagenda pendiente", color = purple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Text("Tu doctor propone una nueva fecha", color = colors.text, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(purple)
                .clickable(onClick = onRespond)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Ver propuesta", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConfirmedActions(appt: Appointment, onCancel: () -> Unit, onReschedule: () -> Unit) {
    val colors = AppTheme.colors
    val now = kotlin.time.Clock.System.now()
    val daysUntil = (appt.startsAt - now).inWholeDays

    if (appt.visitType == VisitType.VIRTUAL) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Lucide.Video, contentDescription = null, tint = colors.navy, modifier = Modifier.size(14.dp))
            Text("Enlace disponible", color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    if (daysUntil >= 3) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlineAction("Reagendar", colors.infoBg, colors.text, Modifier.weight(1f), onClick = onReschedule)
            OutlineAction("Cancelar", colors.red, colors.red, Modifier.weight(1f), onClick = onCancel)
        }
        Text(
            text = appt.startsAt.let {
                val dt = it.toLocalDateTime(TimeZone.currentSystemDefault())
                val cutoff = dt.date.minus(3, DateTimeUnit.DAY)
                "Cancelación gratuita hasta ${cutoff.day} ${monthAbbr(cutoff.month.number)}"
            },
            color = colors.text,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun OutlineAction(text: String, borderColor: Color, foreground: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .border(1.dp, borderColor, shape)
            .clip(shape)
            .background(AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = foreground, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusBadge(text: String, background: Color, foreground: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = foreground, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

private val AppointmentsTab.label: String
    get() = when (this) {
        AppointmentsTab.ACTIVE -> "Activas"
        AppointmentsTab.NEEDS_CLOSURE -> "Por confirmar"
        AppointmentsTab.COMPLETED -> "Completadas"
        AppointmentsTab.CANCELLED -> "Canceladas"
    }

private data class StatusUi(
    val label: String,
    val background: Color,
    val foreground: Color,
)

@Composable
private fun statusUi(status: AppointmentStatus): StatusUi {
    val colors = AppTheme.colors
    return when (status) {
        AppointmentStatus.PENDING_PAYMENT -> StatusUi("PAGO PENDIENTE", colors.amberBg, colors.amber)
        AppointmentStatus.CONFIRMED, AppointmentStatus.SCHEDULED -> StatusUi("CONFIRMADA", colors.greenBg, colors.green)
        AppointmentStatus.COMPLETED -> StatusUi("COMPLETADA", colors.tealBg, colors.teal)
        AppointmentStatus.CANCELLED_BY_PATIENT, AppointmentStatus.CANCELLED_BY_DOCTOR -> StatusUi("CANCELADA", colors.redBg, colors.red)
        else -> StatusUi(status.name, colors.navyTint, colors.navy)
    }
}

private fun doctorLine(appt: Appointment): String {
    val name = appt.doctorName?.let { "Dr. $it" } ?: "Doctor"
    val spec = appt.specialtyName
    return if (spec != null) "$name · $spec" else name
}

private fun appointmentDateLine(appt: Appointment): String {
    val dt = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val dow = dayAbbr(dt.dayOfWeek.ordinal)
    val date = "$dow ${dt.day} ${monthAbbr(dt.month.number)}"
    return "$date · ${formatHour(dt.hour, dt.minute)} · ${visitTypeLabel(appt.visitType)}"
}

private fun dayAbbr(ordinal: Int) = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")[ordinal]

private fun monthAbbr(m: Int) = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")[m - 1]

private fun timeLabel(instant: kotlin.time.Instant): String {
    val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return formatHour(dt.hour, dt.minute)
}

private fun remainingLabel(deadline: kotlin.time.Instant): String? {
    val left = deadline - kotlin.time.Clock.System.now()
    if (left.isNegative()) return null
    val hours = left.inWholeHours
    val minutes = left.inWholeMinutes % 60
    return when {
        hours > 0 -> "vence en ${hours}h ${minutes}m"
        minutes > 0 -> "vence en ${minutes}m"
        else -> "vence en menos de 1m"
    }
}

private fun formatHour(hour: Int, minute: Int): String {
    val period = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "${displayHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $period"
}

private fun visitTypeLabel(visitType: VisitType): String = when (visitType) {
    VisitType.VIRTUAL -> "Telemedicina"
    VisitType.HOME -> "Visita a domicilio"
    VisitType.CLINIC -> "Consulta presencial"
}
