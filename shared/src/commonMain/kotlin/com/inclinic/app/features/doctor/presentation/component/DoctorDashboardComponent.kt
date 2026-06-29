package com.inclinic.app.features.doctor.presentation.component

import com.arkivanov.decompose.value.Value

interface DoctorDashboardComponent {
    val state: Value<DoctorDashboardState>

    fun onRefresh()
    fun onNavigateToSchedule()
    fun onNavigateToPendingAppointments()
    fun onNavigateToNotifications()

    fun onAppointmentTap(appointmentId: String)
    fun onCreateMedicalRecord()
    fun onNavigateToPackages()
    fun onNavigateToPatients()
    fun onNavigateToIncome()

    sealed interface Output {
        data object NavigateToSchedule : Output
        data object NavigateToPendingAppointments : Output
        data object NavigateToNotifications : Output
        data class NavigateToAppointmentDetail(val appointmentId: String) : Output
        data object NavigateToCreateMedicalRecord : Output
        data object NavigateToPackages : Output
        data object NavigateToPatients : Output
        data object NavigateToIncome : Output
    }
}

data class DoctorDashboardState(
    val isLoading: Boolean = false,
    val todayCount: Int = 0,
    val pendingCount: Int = 0,
    val monthlyEarnings: String = "S/0",
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val completedCount: Int = 0,
    val completedThisMonth: Int = 0,
    val patientsCount: Int = 0,
    val recurringPatientsCount: Int = 0,
    val completedTodayPct: Int = 0,
    val upcomingAppointments: List<com.inclinic.app.core.model.Appointment> = emptyList(),
    val error: String? = null,
)
