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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.CalendarClock
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Video
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.patient.presentation.component.DayLevel
import com.inclinic.app.features.patient.presentation.component.RescheduleAppointmentComponent
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.InfoBanner
import com.inclinic.app.ui.atoms.InfoBannerTone
import com.inclinic.app.ui.theme.AppTheme
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)
private val DAY_HEADERS = listOf("LU", "MA", "MI", "JU", "VI", "SA", "DO")

@Composable
fun RescheduleAppointmentScreen(component: RescheduleAppointmentComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    Column(modifier.fillMaxSize().background(colors.sand)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppBackButton(onClick = component::onBack)
            Text("Reagendar Cita", color = colors.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.navy)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Info banner
                InfoBanner(
                    title = "Solo puedes reagendar 1 vez",
                    description = "La nueva fecha debe ser al menos 3 días después de hoy.",
                    tone = InfoBannerTone.Warning,
                    icon = Lucide.CalendarClock,
                )

                // Doctor card
                val appt = state.appointment
                if (appt != null) {
                    val initials = appt.doctorName?.split(" ")
                        ?.mapNotNull { it.firstOrNull()?.uppercase() }
                        ?.take(2)?.joinToString("") ?: "DR"
                    val visitLabel = when (appt.visitType) {
                        VisitType.VIRTUAL -> "Telemedicina"
                        VisitType.HOME -> "Visita a domicilio"
                        VisitType.CLINIC -> "Consulta presencial"
                    }
                    val dt = appt.startsAt.toLocalDateTime(TimeZone.currentSystemDefault())
                    val dateStr = "${dt.day} ${MONTH_NAMES[dt.month.number - 1].take(3)}, ${formatTimeReschedule(dt.hour, dt.minute)}"

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppTheme.colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(colors.navy),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(initials, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "Dr. ${appt.doctorName ?: ""}",
                                        color = colors.text,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    appt.specialtyName?.let {
                                        Text(it, color = colors.muted, fontSize = 11.sp)
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.amberBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Text("ACTUAL", color = colors.amber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.Calendar, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                                Text(dateStr, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Lucide.Video, contentDescription = null, tint = colors.muted, modifier = Modifier.size(14.dp))
                                Text(visitLabel, color = colors.text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Calendar
                RescheduleMonthCalendar(
                    displayMonth = state.displayMonth,
                    today = today,
                    selectedDate = state.selectedDate,
                    dayLevels = state.dayLevels,
                    isLoadingMonth = state.isLoadingMonth,
                    onPrevMonth = component::onPrevMonth,
                    onNextMonth = component::onNextMonth,
                    onDateSelected = component::onDateSelected,
                )

                // Slots — elevated card matching design
                if (state.isLoadingSlots) {
                    Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.navy)
                    }
                } else if (state.selectedDate != null && state.slots.isEmpty()) {
                    Text(
                        "No hay horarios disponibles para esta fecha",
                        textAlign = TextAlign.Center,
                        color = colors.muted,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    )
                } else if (state.slots.isNotEmpty()) {
                    val dateLabel = state.selectedDate?.let { d ->
                        val dow = listOf("Lun","Mar","Mié","Jue","Vie","Sáb","Dom")[d.dayOfWeek.ordinal]
                        val mon = MONTH_NAMES[d.month.number - 1].take(3)
                        "$dow ${d.day} $mon"
                    } ?: ""
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.elevated)
                            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            "Horarios disponibles · $dateLabel",
                            color = colors.text,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        RescheduleSlotGrid(
                            slots = state.slots,
                            selectedSlot = state.selectedSlot,
                            onSlotSelected = component::onSlotSelected,
                        )
                    }
                }

                // Error
                state.error?.let { err ->
                    Text(err, color = colors.red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // CTA — top divider matches design strokeWidth:{top:1}
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
            ) {
                val canConfirm = state.selectedSlot != null && !state.isRescheduling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (canConfirm) colors.navy else colors.navy.copy(alpha = 0.4f))
                        .then(if (canConfirm) Modifier.clickable(onClick = component::onConfirmReschedule) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isRescheduling) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Confirmar reagenda", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RescheduleMonthCalendar(
    displayMonth: LocalDate,
    today: LocalDate,
    selectedDate: LocalDate?,
    dayLevels: Map<LocalDate, DayLevel>,
    isLoadingMonth: Boolean,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val colors = AppTheme.colors
    val year = displayMonth.year
    val month = displayMonth.month.number
    val monthLabel = "${MONTH_NAMES[month - 1]} $year"
    val firstDay = LocalDate(year, month, 1)
    val startOffset = firstDay.dayOfWeek.ordinal
    val daysInMonth = daysInMonthCalc(year, month)
    val minDate = LocalDate(today.year, today.month.number, today.day)

    val cells = buildList<LocalDate?> {
        repeat(startOffset) { add(null) }
        for (d in 1..daysInMonth) add(LocalDate(year, month, d))
        while (size % 7 != 0) add(null)
    }

    val currentMonthStart = LocalDate(today.year, today.month.number, 1)
    val canGoPrev = displayMonth > currentMonthStart

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrevMonth, enabled = canGoPrev) {
                Text("‹", style = MaterialTheme.typography.titleLarge, color = if (canGoPrev) colors.text else Color.LightGray)
            }
            Text(monthLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = colors.navy)
            IconButton(onClick = onNextMonth) {
                Text("›", style = MaterialTheme.typography.titleLarge, color = colors.text)
            }
        }

        Row(Modifier.fillMaxWidth()) {
            DAY_HEADERS.forEach { header ->
                Text(
                    header,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (date != null) {
                            val isPast = date <= today
                            val isSelected = date == selectedDate
                            val level = dayLevels[date]
                            val unknown = level == null && isLoadingMonth
                            val bgColor = when {
                                isSelected -> colors.navy
                                isPast -> Color.Transparent
                                unknown -> Color.Transparent
                                level == DayLevel.OPEN -> colors.lav.copy(alpha = 0.25f)
                                level == DayLevel.FEW -> colors.amberBg
                                else -> colors.base
                            }
                            val textColor = when {
                                isSelected -> Color.White
                                isPast -> Color.LightGray
                                level == DayLevel.OPEN || level == DayLevel.FEW -> colors.text
                                unknown -> colors.text
                                else -> Color.LightGray
                            }
                            val canClick = !isPast && (level == DayLevel.OPEN || level == DayLevel.FEW || unknown)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(2.dp)
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .clickable(enabled = canClick) { onDateSelected(date) },
                            ) {
                                Text(
                                    date.day.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun RescheduleSlotGrid(
    slots: List<AvailabilitySlot>,
    selectedSlot: AvailabilitySlot?,
    onSlotSelected: (AvailabilitySlot) -> Unit,
) {
    val colors = AppTheme.colors
    val columns = 4
    slots.chunked(columns).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { slot ->
                val isSelected = slot.id == selectedSlot?.id
                val bgColor = when {
                    isSelected -> colors.navy
                    !slot.isAvailable -> colors.base
                    else -> colors.surface
                }
                val borderColor = when {
                    isSelected -> colors.navy
                    !slot.isAvailable -> Color.LightGray
                    else -> colors.navy.copy(alpha = 0.4f)
                }
                val textColor = when {
                    isSelected -> Color.White
                    !slot.isAvailable -> Color.LightGray
                    else -> colors.navy
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = slot.isAvailable) { onSlotSelected(slot) }
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                ) {
                    Text(slot.startTime, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = textColor)
                }
            }
            repeat(columns - row.size) { Box(Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun formatTimeReschedule(hour: Int, minute: Int): String {
    val period = if (hour >= 12) "p.m." else "a.m."
    val h = when { hour == 0 -> 12; hour > 12 -> hour - 12; else -> hour }
    return "${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $period"
}

private fun daysInMonthCalc(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}
