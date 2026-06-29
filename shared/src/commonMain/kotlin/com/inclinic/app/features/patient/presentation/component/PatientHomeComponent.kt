package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.Doctor

interface PatientHomeComponent {
    val state: Value<PatientHomeState>

    fun onRefresh()
    fun onSearchTapped()
    fun onDoctorTapped(doctorId: String)
    fun onErrorDismissed()

    fun onAssistantChatTapped()
    fun onAppointmentsTapped()
    fun onAppointmentDetailTapped(appointmentId: String)
    fun onProfileTapped()
    fun onPackagesTapped()
    fun onPremiumTapped()
    fun onNavigateToHistoryAccess()

    sealed interface Output {
        data object NavigateToSearch : Output
        data class NavigateToDoctorProfile(val doctorId: String) : Output
        data object NavigateToAssistantChat : Output
        data object NavigateToAppointments : Output
        data class NavigateToAppointmentDetail(val appointmentId: String) : Output
        data object NavigateToProfile : Output
        data object NavigateToPackages : Output
        data object NavigateToPremium : Output
        data object NavigateToHistoryAccess : Output
    }
}

data class PatientHomeState(
    val isLoading: Boolean = false,
    val upcomingCount: Int = 0,
    val recentDoctors: List<Doctor> = emptyList(),
    val nextAppointment: Appointment? = null,
    val error: String? = null,
)
