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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.composables.icons.lucide.Building2
import com.composables.icons.lucide.Lucide
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.features.patient.presentation.component.AvailabilityCalendarComponent
import com.inclinic.app.features.patient.presentation.component.DayLevel
import com.inclinic.app.ui.atoms.AppBackButton
import com.inclinic.app.ui.atoms.ErrorBanner
import com.inclinic.app.ui.theme.AppTheme
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private val MONTH_NAMES = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre",
)
private val DAY_HEADERS = listOf("LU", "MA", "MI", "JU", "VI", "SÁ", "DO")

@Composable
fun AvailabilityCalendarScreen(component: AvailabilityCalendarComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val colors = AppTheme.colors
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val availableCount = state.slots.count { it.isAvailable }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.sand),
    ) {
        // Header: back + title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBackButton(onClick = component::onBack)
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "Fecha y Hora",
                    color = colors.text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(48.dp))
        }

        // Progress bar — step 2 of 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.navy))
            Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.navy))
            Box(Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.border))
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            state.error?.let {
                ErrorBanner(message = it, onDismiss = {})
                Spacer(Modifier.height(8.dp))
            }

            // Consult type info chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.elevated)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Lucide.Building2,
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "Consulta presencial",
                    color = colors.muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(16.dp))

            MonthCalendar(
                displayMonth = state.displayMonth,
                today = today,
                selectedDate = state.selectedDate,
                dayLevels = state.dayLevels,
                isLoadingMonth = state.isLoadingMonth,
                onPrevMonth = component::onPrevMonth,
                onNextMonth = component::onNextMonth,
                onDateSelected = component::onDateSelected,
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(color = colors.lav.copy(alpha = 0.35f), label = "Disponible")
                LegendItem(color = colors.amberBg, label = "Pocos slots")
                LegendItem(color = colors.base, label = "Sin horario")
            }

            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(colors.border))
            Spacer(Modifier.height(16.dp))

            // Section label
            Text(
                "HORARIOS DISPONIBLES",
                color = colors.muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
            )

            Spacer(Modifier.height(12.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.navy)
                }
            } else if (state.slots.isEmpty()) {
                Text(
                    "No hay horarios disponibles para esta fecha",
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    color = colors.muted,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                )
            } else {
                // Group by time of day when slot times are parseable (morning < 12:00).
                // If no slot time can be parsed, fall back to a single flat grid.
                val morning = state.slots.filter { slotIsMorning(it.startTime) == true }
                val afternoon = state.slots.filter { slotIsMorning(it.startTime) == false }
                val canGroup = morning.isNotEmpty() && afternoon.isNotEmpty()

                if (canGroup) {
                    Text(
                        "Mañana",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    Spacer(Modifier.height(8.dp))
                    SlotGrid(
                        slots = morning,
                        selectedSlot = state.selectedSlot,
                        onSlotSelected = component::onSlotSelected,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tarde",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text,
                    )
                    Spacer(Modifier.height(8.dp))
                    SlotGrid(
                        slots = afternoon,
                        selectedSlot = state.selectedSlot,
                        onSlotSelected = component::onSlotSelected,
                    )
                } else {
                    SlotGrid(
                        slots = state.slots,
                        selectedSlot = state.selectedSlot,
                        onSlotSelected = component::onSlotSelected,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            val enabled = state.selectedSlot != null
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("calendar_continue_button")
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (enabled) colors.navy else colors.navy.copy(alpha = 0.5f))
                    .clickable(enabled = enabled, onClick = component::onContinue),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (enabled) "Confirmar Horario" else "Selecciona un horario",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun MonthCalendar(
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
    val startOffset = firstDay.dayOfWeek.ordinal // Mon=0 … Sun=6
    val daysInMonth = daysInMonth(year, month)

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
                Text(
                    "‹",
                    fontSize = 22.sp,
                    color = if (canGoPrev) colors.text else colors.light,
                )
            }
            Text(monthLabel, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.navy)
            IconButton(onClick = onNextMonth) {
                Text("›", fontSize = 22.sp, color = colors.text)
            }
        }

        Row(Modifier.fillMaxWidth()) {
            DAY_HEADERS.forEach { header ->
                Text(
                    header,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp,
                    color = colors.muted,
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (date != null) {
                            DayCell(
                                date = date,
                                today = today,
                                isSelected = date == selectedDate,
                                level = dayLevels[date],
                                isLoading = isLoadingMonth,
                                onClick = { if (date >= today) onDateSelected(date) },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    today: LocalDate,
    isSelected: Boolean,
    level: DayLevel?,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val isPast = date < today
    val isToday = date == today
    val unknown = level == null && isLoading

    val bgColor = when {
        isSelected -> colors.navy
        isPast -> Color.Transparent
        unknown -> Color.Transparent
        level == DayLevel.OPEN -> colors.lav.copy(alpha = 0.25f)
        level == DayLevel.FEW -> colors.amberBg
        else -> colors.base // NONE or null after load (no schedule that day)
    }
    val textColor = when {
        isSelected -> Color.White
        isPast -> colors.light
        level == DayLevel.OPEN || level == DayLevel.FEW -> colors.text
        unknown -> colors.text
        else -> colors.light
    }
    val borderMod = if (isToday && !isSelected) {
        Modifier.border(1.5.dp, colors.navy, RoundedCornerShape(8.dp))
    } else {
        Modifier
    }

    val clickable = !isPast && (level == DayLevel.OPEN || level == DayLevel.FEW || unknown)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(2.dp)
            .size(36.dp)
            .then(if (clickable) Modifier.testTag("calendar_day") else Modifier)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .then(borderMod)
            .clickable(enabled = clickable, onClick = onClick),
    ) {
        Text(
            date.day.toString(),
            fontSize = 13.sp,
            color = textColor,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun SlotGrid(
    slots: List<AvailabilitySlot>,
    selectedSlot: AvailabilitySlot?,
    onSlotSelected: (AvailabilitySlot) -> Unit,
) {
    val columns = 4
    slots.chunked(columns).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { slot ->
                SlotChip(
                    slot = slot,
                    isSelected = slot.id == selectedSlot?.id,
                    onClick = { onSlotSelected(slot) },
                    modifier = Modifier.weight(1f),
                )
            }
            repeat(columns - row.size) { Box(Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SlotChip(
    slot: AvailabilitySlot,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val bgColor = when {
        isSelected -> colors.navy
        !slot.isAvailable -> colors.sand
        else -> colors.elevated
    }
    val textColor = when {
        isSelected -> Color.White
        !slot.isAvailable -> colors.muted
        else -> colors.navy
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .testTag("slot_chip")
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .clickable(enabled = slot.isAvailable, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 16.dp),
    ) {
        Text(
            slot.startTime,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
                .border(1.dp, colors.light, RoundedCornerShape(4.dp)),
        )
        Text(label, fontSize = 11.sp, color = colors.muted)
    }
}

/**
 * Classifies a slot's start time as morning (true) or afternoon (false).
 * Returns null when the time string can't be parsed so the caller can skip grouping.
 * Handles "9:00", "14:00", "2:00 PM", "12:30 AM" style strings.
 */
private fun slotIsMorning(startTime: String): Boolean? {
    val raw = startTime.trim()
    val upper = raw.uppercase()
    val hasPm = upper.contains("PM")
    val hasAm = upper.contains("AM")
    val hour = upper
        .replace("AM", "")
        .replace("PM", "")
        .trim()
        .substringBefore(":")
        .toIntOrNull() ?: return null

    val hour24 = when {
        hasPm -> if (hour == 12) 12 else hour + 12
        hasAm -> if (hour == 12) 0 else hour
        else -> hour
    }
    return hour24 < 12
}

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    else -> 30
}
