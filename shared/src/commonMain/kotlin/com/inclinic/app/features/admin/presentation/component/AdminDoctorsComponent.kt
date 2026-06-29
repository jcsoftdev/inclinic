package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminDoctorListItem

/** Filter tabs for the admin doctors list — mirrors design filters. */
enum class AdminDoctorsFilter(val label: String, val apiStatus: String?) {
    All("Todos", null),
    Active("Activos", "ACTIVE"),
    Pending("En revisión", null),      // navigates to pending-approvals screen
    Suspended("Suspendidos", "SUSPENDED"),
}

interface AdminDoctorsComponent {
    val state: Value<AdminDoctorsState>

    fun onRefresh()
    fun onSearchQueryChange(query: String)
    fun onFilterChange(filter: AdminDoctorsFilter)
    fun onDoctorClicked(doctorId: String)
    fun onNavigateToPendingApprovals()

    sealed interface Output {
        data class NavigateToDetail(val doctorId: String) : Output
        data object NavigateToPendingApprovals : Output
    }
}

data class AdminDoctorsState(
    val allItems: List<AdminDoctorListItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: AdminDoctorsFilter = AdminDoctorsFilter.All,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val visibleItems: List<AdminDoctorListItem>
        get() {
            val base = when (activeFilter) {
                AdminDoctorsFilter.All -> allItems
                AdminDoctorsFilter.Active -> allItems.filter { it.isActive && !it.user.isSuspended }
                AdminDoctorsFilter.Suspended -> allItems.filter { it.user.isSuspended }
                AdminDoctorsFilter.Pending -> allItems // nav to pending screen — handled in component
            }
            return if (searchQuery.isBlank()) base
            else base.filter { item ->
                val q = searchQuery.lowercase()
                item.fullName.lowercase().contains(q) ||
                    item.user.email.lowercase().contains(q) ||
                    item.primarySpecialty.lowercase().contains(q)
            }
        }
}
