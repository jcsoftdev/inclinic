package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.infrastructure.remote.DaySummary
import kotlinx.datetime.LocalDate

interface WeeklyScheduleComponent {
    val state: Value<WeeklyScheduleState>

    fun onPreviousWeek()
    fun onNextWeek()
    fun onDayTap(date: LocalDate)
    fun onBack()

    sealed interface Output {
        data class NavigateToDailySchedule(val date: LocalDate) : Output
        data object Back : Output
    }
}

data class WeeklyScheduleState(
    val weekStart: LocalDate? = null,
    val daySummaries: List<DaySummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
