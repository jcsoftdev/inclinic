package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.AvailabilitySlot
import kotlinx.datetime.LocalDate

enum class DayLevel { OPEN, FEW, NONE }

interface AvailabilityCalendarComponent {
    val state: Value<AvailabilityCalendarState>

    fun onDateSelected(date: LocalDate)
    fun onSlotSelected(slot: AvailabilitySlot)
    fun onPrevMonth()
    fun onNextMonth()
    fun onContinue()
    fun onBack()

    sealed interface Output {
        data class NavigateToBooking(val doctorId: String, val slotId: String, val date: String, val startTime: String) : Output
        data object Back : Output
    }
}

data class AvailabilityCalendarState(
    val doctorId: String = "",
    val displayMonth: LocalDate = LocalDate(2026, 1, 1),
    val selectedDate: LocalDate? = null,
    val slots: List<AvailabilitySlot> = emptyList(),
    val selectedSlot: AvailabilitySlot? = null,
    val isLoading: Boolean = false,
    val isLoadingMonth: Boolean = false,
    val dayLevels: Map<LocalDate, DayLevel> = emptyMap(),
    val error: String? = null,
)
