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
    /**
     * True once the first successful load has completed. Replaces the old
     * `appointmentsToday != 0 || pendingDoctors != 0` sentinel, which misread a
     * genuinely-zero successful load as "no data yet" and rendered the full
     * [AdminDashboardViewState.Failed] screen on a subsequent refresh error
     * instead of the dismissible-banner-over-data [AdminDashboardViewState.Loaded] state.
     */
    val hasLoadedOnce: Boolean = false,

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

/**
 * Pure, Compose-free rendering decision for [com.inclinic.app.features.admin.presentation.ui.AdminDashboardScreen].
 *
 * Previously a load failure with no data yet (still-zeroed KPIs) only showed a dismissible
 * retry banner floating over a grid of literal zeros — that read as "loaded, nothing's
 * happening" rather than "failed to load". [Failed] gives that combination its own
 * full KPI-grid-shaped state with a retry affordance, distinct from [Loaded] (which still
 * covers a background-refresh failure *after* real data has already rendered once — the
 * dismissible banner-over-data behavior is preserved there).
 */
sealed interface AdminDashboardViewState {
    data object Loading : AdminDashboardViewState
    data class Failed(val message: String) : AdminDashboardViewState
    data object Loaded : AdminDashboardViewState
}

fun AdminDashboardState.toViewState(): AdminDashboardViewState = when {
    isLoading && !hasLoadedOnce -> AdminDashboardViewState.Loading
    error != null && !hasLoadedOnce -> AdminDashboardViewState.Failed(error)
    else -> AdminDashboardViewState.Loaded
}
