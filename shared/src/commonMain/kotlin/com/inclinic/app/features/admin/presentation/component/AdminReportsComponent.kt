package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminReportItem

/**
 * Filter chips for the reports list.
 *
 * Pendientes → status=PENDING (server-side filter)
 * Revisados  → status=ALL then client-side: REVIEWED | ACTION_TAKEN | DISMISSED
 * Todos      → status=ALL (no client filter)
 */
enum class AdminReportsFilter(val label: String, val apiStatus: String?) {
    Pending("Pendientes", "PENDING"),
    Resolved("Revisados", "ALL"),    // server returns all; client filters non-PENDING
    All("Todos", "ALL"),             // server returns all; no client filter
}

interface AdminReportsComponent {
    val state: Value<AdminReportsState>

    fun onRefresh()
    fun onFilterChange(filter: AdminReportsFilter)
    fun onReportClicked(report: AdminReportItem)
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data class NavigateToResolve(val report: AdminReportItem) : Output
    }
}

data class AdminReportsState(
    val allItems: List<AdminReportItem> = emptyList(),
    val activeFilter: AdminReportsFilter = AdminReportsFilter.Pending,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val visibleItems: List<AdminReportItem>
        get() = when (activeFilter) {
            AdminReportsFilter.Pending  -> allItems   // server already filtered to PENDING
            AdminReportsFilter.Resolved -> allItems.filter { it.isResolved }
            AdminReportsFilter.All      -> allItems
        }
}
