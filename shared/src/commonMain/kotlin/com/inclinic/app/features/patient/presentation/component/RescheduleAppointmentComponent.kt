package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AvailabilitySlot
import kotlinx.datetime.LocalDate

interface RescheduleAppointmentComponent {
    val state: Value<RescheduleAppointmentState>

    fun onDateSelected(date: LocalDate)
    fun onSlotSelected(slot: AvailabilitySlot)
    fun onPrevMonth()
    fun onNextMonth()
    fun onConfirmReschedule()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object Rescheduled : Output
    }
}

data class RescheduleAppointmentState(
    val appointment: Appointment? = null,
    val displayMonth: LocalDate = LocalDate(2026, 1, 1),
    val selectedDate: LocalDate? = null,
    val slots: List<AvailabilitySlot> = emptyList(),
    val selectedSlot: AvailabilitySlot? = null,
    val dayLevels: Map<LocalDate, DayLevel> = emptyMap(),
    val isLoading: Boolean = false,
    val isLoadingMonth: Boolean = false,
    val isLoadingSlots: Boolean = false,
    val isRescheduling: Boolean = false,
    val error: String? = null,
)
