package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminTopDoctor

interface AdminFinanceComponent {
    val state: Value<AdminFinanceState>

    fun onRefresh()
    fun onBack()
    fun onExport()

    /**
     * Called by the screen after it has handled [AdminFinanceState.exportBytes]
     * (e.g. opened the FileSaver share sheet). Clears the bytes from state and
     * sets a success [AdminFinanceState.exportMessage].
     */
    fun onExportHandled()

    sealed interface Output {
        data object Back : Output
    }
}

data class AdminFinanceState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /**
     * True once the first successful load has completed. Replaces the old
     * `balanceTotal != "S/ 0"` sentinel, which misread a genuinely-zero successful
     * load as "no data yet" and rendered the full [AdminFinanceViewState.Failed]
     * screen on a subsequent refresh error instead of the dismissible-banner-over-data
     * [AdminFinanceViewState.Loaded] state.
     */
    val hasLoadedOnce: Boolean = false,

    // Hero card
    val balanceTotal: String = "S/ 0",
    val released: String = "S/ 0",
    val held: String = "S/ 0",

    // Metric tiles
    val thisMonthRevenue: String = "S/ 0",
    val heldAmount: String = "S/ 0",
    val heldCount: Int = 0,

    // Movimientos
    val topDoctors: List<AdminTopDoctor> = emptyList(),

    // Export
    val isExporting: Boolean = false,
    /**
     * Raw CSV bytes ready to be saved/shared via [FileSaver].
     * Non-null only between the moment the bytes are fetched and when the screen
     * consumes them via [AdminFinanceComponent.onExportHandled].
     * ByteArray uses reference equality so a new fetch always triggers LaunchedEffect.
     */
    val exportBytes: ByteArray? = null,
    /** Feedback message shown in the snackbar after export completes or fails. */
    val exportMessage: String? = null,
)

/**
 * Pure, Compose-free rendering decision for [com.inclinic.app.features.admin.presentation.ui.AdminFinanceScreen].
 *
 * Mirrors [AdminDashboardViewState]: a load failure before any real balance is known
 * gets its own full [Failed] state with a retry affordance, instead of silently
 * rendering the hero card + KPI tiles + empty movements list at their zeroed defaults
 * with just an inline red line of text at the top.
 */
sealed interface AdminFinanceViewState {
    data object Loading : AdminFinanceViewState
    data class Failed(val message: String) : AdminFinanceViewState
    data object Loaded : AdminFinanceViewState
}

fun AdminFinanceState.toViewState(): AdminFinanceViewState = when {
    isLoading && !hasLoadedOnce -> AdminFinanceViewState.Loading
    error != null && !hasLoadedOnce -> AdminFinanceViewState.Failed(error)
    else -> AdminFinanceViewState.Loaded
}
