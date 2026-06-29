package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem

/**
 * Filter chips for the patients list.
 *
 * Todos  → all patients (no status filter)
 * Premium → subscriptionTier == PREMIUM (client-side filter on full list)
 * Observados → NO backing field on GET /api/admin/patients-list; always empty. Documented gap.
 */
enum class AdminPatientsFilter(val label: String, val apiStatus: String?) {
    All("Todos", null),
    Premium("Premium", null),       // client-side: tier == PREMIUM
    Observed("Observados", null),   // GAP: no "flagged" field on patients list
    Suspended("Suspendidos", "SUSPENDED"),
}

interface AdminPatientsComponent {
    val state: Value<AdminPatientsState>

    fun onRefresh()
    fun onSearchQueryChange(query: String)
    fun onFilterChange(filter: AdminPatientsFilter)
    fun onPatientClicked(patient: AdminPatientListItem)
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data class NavigateToDetail(val patient: AdminPatientListItem) : Output
    }
}

data class AdminPatientsState(
    val allItems: List<AdminPatientListItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: AdminPatientsFilter = AdminPatientsFilter.All,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val visibleItems: List<AdminPatientListItem>
        get() {
            val base: List<AdminPatientListItem> = when (activeFilter) {
                AdminPatientsFilter.All       -> allItems
                AdminPatientsFilter.Premium   -> allItems.filter { it.subscriptionTier == "PREMIUM" }
                AdminPatientsFilter.Observed  -> emptyList() // gap: no observed flag
                AdminPatientsFilter.Suspended -> allItems.filter { it.isSuspended }
            }
            return if (searchQuery.isBlank()) base
            else base.filter { p ->
                val q = searchQuery.lowercase()
                p.fullName.lowercase().contains(q) || p.email.lowercase().contains(q)
            }
        }

    val isGapFilter: Boolean
        get() = activeFilter == AdminPatientsFilter.Observed
}
