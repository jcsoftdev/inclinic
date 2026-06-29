package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import kotlinx.datetime.LocalDate

interface DailyScheduleComponent {
    val state: Value<DailyScheduleState>

    fun onPreviousDay()
    fun onNextDay()
    fun onAppointmentTap(appointmentId: String)
    fun onOpenRescheduleQueue()
    fun onBack()

    sealed interface Output {
        data class NavigateToAppointmentDetail(val appointmentId: String) : Output
        data object OpenRescheduleQueue : Output
        data object Back : Output
    }
}

data class DailyScheduleState(
    val date: LocalDate? = null,
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
