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
