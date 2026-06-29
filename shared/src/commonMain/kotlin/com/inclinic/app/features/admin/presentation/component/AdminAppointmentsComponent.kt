package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentListItem
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Filter options for the admin appointments list — mirrors design tabs. */
enum class AdminAppointmentsFilter(val label: String) {
    All("Todas"),
    Today("Hoy"),
    Held("Retenidas"),
    Cancelled("Canceladas"),
}

interface AdminAppointmentsComponent {
    val state: Value<AdminAppointmentsState>

    fun onRefresh()
    fun onSearchQueryChange(query: String)
    fun onFilterChange(filter: AdminAppointmentsFilter)
    fun onAppointmentClicked(appointmentId: String)

    sealed interface Output {
        data class NavigateToDetail(val appointmentId: String) : Output
    }
}

data class AdminAppointmentsState(
    val allItems: List<AdminAppointmentListItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: AdminAppointmentsFilter = AdminAppointmentsFilter.All,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val visibleItems: List<AdminAppointmentListItem>
        get() {
            val todayStr = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString() // "YYYY-MM-DD"
            val base = when (activeFilter) {
                AdminAppointmentsFilter.All -> allItems
                AdminAppointmentsFilter.Today -> allItems.filter { it.startTime.startsWith(todayStr) }
                AdminAppointmentsFilter.Held -> allItems.filter { !it.paymentHoldStatus.isNullOrBlank() }
                AdminAppointmentsFilter.Cancelled -> allItems.filter { it.status == "CANCELLED" }
            }
            return if (searchQuery.isBlank()) base
            else base.filter { item ->
                val q = searchQuery.lowercase()
                item.patient.fullName.lowercase().contains(q) ||
                    item.doctor.fullName.lowercase().contains(q) ||
                    item.specialty.name.lowercase().contains(q) ||
                    item.id.lowercase().contains(q)
            }
        }
}
