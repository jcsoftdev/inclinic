@file:Suppress("DEPRECATION")

package com.inclinic.app.features.patient.presentation.ui

import kotlinx.datetime.number

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.FileText
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.ShieldCheck
import com.composables.icons.lucide.Video
import com.composables.icons.lucide.X
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.util.formatDecimal
import com.inclinic.app.features.patient.presentation.component.AppointmentDetailComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun AppointmentDetailScreen(component: AppointmentDetailComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val appt = state.appointment

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        AppointmentDetailHeader(onBack = component::onBack)

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }

            appt == null -> {
                state.error?.let {
                    ErrorBanner(
                        message = it,
                        onDismiss = component::onErrorDismissed,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            }

            else -> {
                val now = kotlin.time.Clock.System.now()
                val isPast = appt.startsAt <= now
                val isPendingPayment = appt.status == AppointmentStatus.PENDING_PAYMENT
                val isPaymentExpired = isPendingPayment && appt.paymentDeadline != null && appt.paymentDeadline <= now
                val isConfirmed = appt.status == AppointmentStatus.CONFIRMED || appt.status == AppointmentStatus.SCHEDULED
                val canModify = !isPast && !isPendingPayment && (appt.startsAt - now).inWholeDays >= 3

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    state.error?.let {
                        ErrorBanner(message = it, onDismiss = component::onErrorDismissed)
                    }

                    when {
                        isPaymentExpired -> ExpiredPaymentBanner()
                        isPast -> PastAppointmentBanner()
                        isPendingPayment -> PendingPaymentBanner(appt)
                        isConfirmed -> ConfirmationBanner(appt)
                    }
                    DoctorSummaryCard(doctor = state.doctor, onChat = component::onChat)
                    AppointmentInfoCard(appt = appt)
                    PaymentSummaryCard(appt = appt)
                }

                if (isPendingPayment && !isPaymentExpired) {
                    PayNowBar(onPayNow = component::onPayNow)
                } else if (canModify) {
                    ActionsBar(
                        onCancel = component::onCancel,
                        onReschedule = component::onReschedule,
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentDetailHeader(onBack: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppBackButton(onClick = onBack)
        Text(
            text = "Mi Cita",
            style = AppTheme.typography.displayNano,
            color = colors.text,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.surface)
                .border(1.dp, colors.border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Share2, contentDescription = null, tint = colors.text, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun PastAppointmentBanner() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.amberBg)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.amber),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Calendar, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Cita finalizada", color = colors.amber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("La hora de esta cita ya pasó", color = colors.amber, fontSize = 12.sp)
        }
    }
}

@Composable
private fun PendingPaymentBanner(appt: Appointment) {
    val colors = AppTheme.colors
    val deadlineText = appt.paymentDeadline?.toLocalDateTime(TimeZone.currentSystemDefault())?.let { ldt ->
        "Paga antes de las ${formatHour(ldt.hour, ldt.minute)} del ${ldt.day} ${monthName(ldt.month.number)}"
    } ?: "Completa el pago para confirmar tu cita"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.amberBg)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.amber),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Calendar, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Pago pendiente", color = colors.amber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(deadlineText, color = colors.amber, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ExpiredPaymentBanner() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.redBg)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.red),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Calendar, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Plazo de pago vencido", color = colors.red, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("La reserva se liberó. Agenda un nuevo horario.", color = colors.red, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ConfirmationBanner(appt: Appointment) {
    val colors = AppTheme.colors
    val dateText = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault()).let { ldt ->
        "Tu doctor te espera el ${ldt.day} de ${monthName(ldt.month.number)}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.greenBg)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.green),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Cita confirmada", color = colors.green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(dateText, color = colors.green, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DoctorSummaryCard(doctor: Doctor?, onChat: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .softCard()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(colors.navyLight),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials(doctor?.fullName ?: "PH"), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = doctor?.fullName ?: "Patricia Huamán",
                color = colors.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = doctorSpecialtyLine(doctor),
                color = colors.muted,
                fontSize = 12.sp,
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onChat),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Lucide.MessageCircle, contentDescription = null, tint = colors.navy, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
private fun AppointmentInfoCard(appt: Appointment) {
    val ldt = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .softCard()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DetailRow(
            icon = Lucide.Calendar,
            title = "${dayName(ldt.dayOfWeek.ordinal)} ${ldt.day} de ${monthName(ldt.month.number)} · ${formatHour(ldt.hour, ldt.minute)}",
            subtitle = "Duración: 45 min",
        )
        DividerLine()
        DetailRow(
            icon = when (appt.visitType) {
                VisitType.VIRTUAL -> Lucide.Video
                VisitType.HOME -> Lucide.House
                VisitType.CLINIC -> Lucide.MapPin
            },
            title = visitTypeLabel(appt.visitType),
            subtitle = locationLabel(appt.visitType),
        )
        DividerLine()
        DetailRow(
            icon = Lucide.FileText,
            title = "Motivo",
            subtitle = appt.notes?.takeIf { it.isNotBlank() } ?: "Plan nutricional y control de peso",
        )
    }
}

@Composable
private fun DetailRow(icon: ImageVector, title: String, subtitle: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, tint = colors.navy, modifier = Modifier.size(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = colors.text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = colors.muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun PaymentSummaryCard(appt: Appointment) {
    val colors = AppTheme.colors
    val isPending = appt.status == AppointmentStatus.PENDING_PAYMENT
    val labelText = if (isPending) "Total a pagar" else "Total pagado"
    val badgeLabel = if (isPending) "Pendiente" else "En custodia"
    val badgeBg = if (isPending) colors.amberBg else colors.greenBg
    val badgeFg = if (isPending) colors.amber else colors.green

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.navyTint)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(labelText, color = colors.muted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.4.sp)
            Text(
                "S/ ${appt.consultationFee.formatDecimal(2)}",
                style = AppTheme.typography.displayXSmall,
                color = colors.navy,
            )
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeBg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Lucide.ShieldCheck, contentDescription = null, tint = badgeFg, modifier = Modifier.size(12.dp))
            Text(badgeLabel, color = badgeFg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PayNowBar(onPayNow: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp, top = 8.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.navy)
            .clickable(onClick = onPayNow),
        contentAlignment = Alignment.Center,
    ) {
        Text("Pagar Ahora", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.border),
    )
}

@Composable
private fun ActionsBar(
    onCancel: () -> Unit,
    onReschedule: () -> Unit,
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionButton(
            label = "Reagendar",
            icon = Lucide.CalendarClock,
            bg = colors.surface,
            fg = colors.text,
            borderColor = colors.border,
            onClick = onReschedule,
            modifier = Modifier.weight(1f),
        )
        ActionButton(
            label = "Cancelar",
            icon = Lucide.X,
            bg = colors.redBg,
            fg = colors.red,
            borderColor = null,
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionButton(
    label: String,
    icon: ImageVector,
    bg: Color,
    fg: Color,
    borderColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(shape)
            .background(bg)
            .then(
                if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier
            )
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Text(label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Modifier.softCard(): Modifier {
    val colors = AppTheme.colors
    return this
        .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x0D000000), spotColor = Color(0x0D000000))
        .clip(RoundedCornerShape(16.dp))
        .background(colors.surface)
}

private fun doctorSpecialtyLine(doctor: Doctor?): String {
    val specialty = doctor?.specialties?.firstOrNull()?.name ?: "Nutrición"
    val rating = doctor?.ratingAverage ?: return specialty
    val count = doctor.ratingsCount
    return "$specialty · ${rating.formatDecimal(1)} ★ ($count)"
}

private fun visitTypeLabel(visitType: VisitType): String = when (visitType) {
    VisitType.VIRTUAL -> "Telemedicina"
    VisitType.HOME -> "Visita a domicilio"
    VisitType.CLINIC -> "Consulta presencial"
}

private fun locationLabel(visitType: VisitType): String = when (visitType) {
    VisitType.VIRTUAL -> "Videollamada segura"
    VisitType.HOME -> "Av. Pardo 123, Dpto 502 · Miraflores"
    VisitType.CLINIC -> "Consultorio principal"
}

private fun monthName(month: Int): String = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
)[month - 1]

private fun dayName(dayOrdinal: Int): String = listOf(
    "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo",
)[dayOrdinal.coerceIn(0, 6)]

private fun formatHour(hour: Int, minute: Int): String {
    val period = if (hour >= 12) "PM" else "AM"
    val h = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
    return "${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $period"
}

private fun initials(name: String): String =
    name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "PH" }
