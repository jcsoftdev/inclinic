package com.inclinic.app.features.admin.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.admin.infrastructure.remote.AdminSpecialtyItem

/** Filter chips for the specialty catalog screen. */
enum class AdminSpecialtiesFilter(val label: String) {
    All("Todas"),
    Active("Activas"),
    /**
     * "En revisión" cannot be backed by catalog data — the public endpoint only
     * returns isActive=true entries and carries no "under review" status field.
     * This chip is displayed but always shows an empty list with a gap note.
     */
    UnderReview("En revisión"),
    /**
     * "Ocultas" = isActive=false. The public GET /api/specialties endpoint filters to
     * isActive=true only, so this chip always shows empty with a gap note.
     * A future admin-specific endpoint would be needed to back it.
     */
    Hidden("Ocultas"),
}

interface AdminSpecialtiesComponent {
    val state: Value<AdminSpecialtiesState>

    fun onRefresh()
    fun onFilterChange(filter: AdminSpecialtiesFilter)
    fun onCreateSpecialty(name: String, description: String?, icon: String?)
    fun onShowCreateDialog()
    fun onDismissCreateDialog()
    fun onOpenRequests()
    fun onBack()

    sealed interface Output {
        data object Back : Output
        data object OpenRequests : Output
    }
}

data class AdminSpecialtiesState(
    val allItems: List<AdminSpecialtyItem> = emptyList(),
    val activeFilter: AdminSpecialtiesFilter = AdminSpecialtiesFilter.All,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val isCreating: Boolean = false,
    val createError: String? = null,
) {
    val visibleItems: List<AdminSpecialtyItem>
        get() = when (activeFilter) {
            AdminSpecialtiesFilter.All        -> allItems
            AdminSpecialtiesFilter.Active     -> allItems.filter { it.isActive }
            AdminSpecialtiesFilter.UnderReview -> emptyList() // gap: not in catalog endpoint
            AdminSpecialtiesFilter.Hidden      -> emptyList() // gap: public endpoint excludes inactive
        }

    val isGapFilter: Boolean
        get() = activeFilter == AdminSpecialtiesFilter.UnderReview ||
                activeFilter == AdminSpecialtiesFilter.Hidden
}
