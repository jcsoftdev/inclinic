package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment

interface PatientAppointmentsListComponent {
    val state: Value<PatientAppointmentsListState>

    fun onTabChange(tab: AppointmentsTab)
    fun onLoadMore()
    fun onRefresh()
    fun onAppointmentTapped(appointmentId: String)
    fun onPayNow(appointmentId: String)
    fun onCancel(appointmentId: String)
    fun onReschedule(appointmentId: String)
    fun onRespondReschedule(appointmentId: String)
    fun onErrorDismissed()
    fun onSearchDoctors()

    sealed interface Output {
        data class NavigateToAppointmentDetail(val appointmentId: String) : Output
        data class NavigateToPayment(val appointmentId: String) : Output
        data class NavigateToCancel(val appointmentId: String) : Output
        data class NavigateToReschedule(val appointmentId: String, val doctorId: String, val consultType: String) : Output
        data class NavigateToRescheduleResponse(val appointmentId: String) : Output
        data object NavigateToSearch : Output
    }
}

enum class AppointmentsTab { ACTIVE, COMPLETED, CANCELLED }

data class PatientAppointmentsListState(
    val selectedTab: AppointmentsTab = AppointmentsTab.ACTIVE,
    val appointments: List<Appointment> = emptyList(),
    val isLoading: Boolean = false,
    val page: Int = 1,
    val hasMore: Boolean = true,
    val error: String? = null,
)
