package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.HistoryAccessLog

interface HistoryAccessLogsComponent {
    val state: Value<HistoryAccessLogsState>
    fun onRefresh()
    fun onBack()
    fun onManageAccess()
    /** User tapped an [HistoryAccessLog] row — drill down to its detail screen. */
    fun onLogClick(entry: HistoryAccessLog)
    sealed interface Output {
        data object Back : Output
        data object NavigateToManageAccess : Output
        data class NavigateToDetail(val entry: HistoryAccessLog) : Output
    }
}

data class HistoryAccessLogsState(
    val logs: List<HistoryAccessLog> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
