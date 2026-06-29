package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.Doctor

interface AppointmentDetailComponent {
    val state: Value<AppointmentDetailState>

    fun onPayNow()
    fun onCancel()
    fun onReschedule()
    fun onChat()
    fun onBack()
    fun onErrorDismissed()

    sealed interface Output {
        data class NavigateToPayment(val appointmentId: String) : Output
        data class NavigateToCancel(val appointmentId: String) : Output
        data class NavigateToReschedule(val appointmentId: String, val doctorId: String, val consultType: String) : Output
        data class NavigateToChat(val doctorId: String, val doctorName: String) : Output
        data class NavigateToRescheduleResponse(val appointmentId: String) : Output
        data object Back : Output
    }
}

data class AppointmentDetailState(
    val appointment: Appointment? = null,
    val doctor: Doctor? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
