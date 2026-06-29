package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem

/**
 * Component for the admin patient appointments list.
 *
 * Loads all appointments for a given [patientId] using the existing
 * [AdminDataSource.getAppointments] filtered by patientId.
 */
interface AdminPatientAppointmentsComponent {
    val state: Value<AdminPatientAppointmentsState>

    fun onRefresh()
    fun onBack()
    fun onAppointmentClicked(appointmentId: String)

    sealed interface Output {
        data object Back : Output
        data class NavigateToDetail(val appointmentId: String) : Output
    }
}

data class AdminPatientAppointmentsState(
    val appointments: List<AdminAppointmentListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
