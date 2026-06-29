package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value

interface AdminDashboardComponent {
    val state: Value<AdminDashboardState>

    fun onRefresh()
    fun onNavigateToNotifications()
    fun onNavigateToDoctorApprovals()
    fun onNavigateToDisputes()
    fun onNavigateToFinance()

    sealed interface Output {
        data object NavigateToNotifications : Output
        data object NavigateToDoctorApprovals : Output
        data object NavigateToDisputes : Output
        data object NavigateToFinance : Output
    }
}

data class AdminDashboardState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // Hero + Citas KPI
    val appointmentsToday: Int = 0,
    val pendingDoctors: Int = 0,

    // Pagos KPI
    val monthRevenue: String = "S/ 0",
    val pendingDisputes: Int = 0,
    val noShowAppointments: Int = 0,

    // Riesgo KPI (disputes + no-shows)
    val riskCount: Int = 0,

    // SLA KPI — no backend field yet; placeholder until ops metrics exist.
    val slaPct: Int = 92,
)
