package com.inclinic.app.features.doctor.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.inclinic.app.features.doctor.presentation.component.WeeklyScheduleComponent
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyScheduleScreen(component: WeeklyScheduleComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda semanal") },
                navigationIcon = {
                    IconButton(onClick = component::onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = component::onPreviousWeek) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Semana anterior")
                }
                Text(state.weekStart?.toString() ?: "", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = component::onNextWeek) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Semana siguiente")
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val weekStart = state.weekStart
                if (weekStart != null) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        (0..6).forEach { offset ->
                            val date = weekStart.plus(offset, DateTimeUnit.DAY)
                            val summary = state.daySummaries.find { it.date == date.toString() }
                            DayCell(
                                date = date,
                                count = summary?.appointmentCount ?: 0,
                                isToday = date == today,
                                onClick = { component.onDayTap(date) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayCell(
    date: LocalDate,
    count: Int,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dayNames = listOf("L", "M", "X", "J", "V", "S", "D")
    val dayIndex = (date.dayOfWeek.ordinal) % 7
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = if (isToday) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors(),
    ) {
        Column(
            Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(dayNames.getOrElse(dayIndex) { "" }, style = MaterialTheme.typography.labelSmall)
            Text(date.day.toString(), style = MaterialTheme.typography.bodySmall)
            if (count > 0) {
                Badge { Text(count.toString()) }
            }
        }
    }
}
