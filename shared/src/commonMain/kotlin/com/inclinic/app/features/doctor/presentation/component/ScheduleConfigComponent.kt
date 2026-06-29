package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import kotlinx.datetime.DayOfWeek

interface ScheduleConfigComponent {
    val state: Value<ScheduleConfigState>

    fun onToggleDay(day: DayOfWeek)
    fun onExpandDay(day: DayOfWeek)
    fun onStartTimeChange(day: DayOfWeek, value: String)
    fun onEndTimeChange(day: DayOfWeek, value: String)
    fun onMaxPatientsChange(day: DayOfWeek, value: String)
    fun onSlotDurationChange(day: DayOfWeek, minutes: Int)
    fun onPriceChange(day: DayOfWeek, value: String)
    fun onToggleAllowNegotiation(day: DayOfWeek)
    fun onSave()
    fun onBack()

    sealed interface Output {
        data object Back : Output
    }
}

/**
 * Editable availability for a single weekday — the design "Editar Horarios" day card.
 */
data class DayScheduleUi(
    val day: DayOfWeek,
    val enabled: Boolean = false,
    val expanded: Boolean = false,
    val startTime: String = "08:00",
    val endTime: String = "13:00",
    val maxPatients: String = "8",
    val slotDuration: Int = 30,
    val price: String = "",
    val allowNegotiation: Boolean = false,
)

data class ScheduleConfigState(
    val days: List<DayScheduleUi> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false,
)
