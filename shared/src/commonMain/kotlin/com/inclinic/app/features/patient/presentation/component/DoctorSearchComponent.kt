package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.Doctor
import com.inclinic.app.core.model.Specialty
import kotlinx.serialization.Serializable

enum class DoctorSortOrder { Recent, TopRated, PriceAsc, PriceDesc }

interface DoctorSearchComponent {
    val state: Value<DoctorSearchState>

    fun onQueryChange(query: String)
    fun onSpecialtyChange(specialty: String?)
    fun onSortChange(sort: DoctorSortOrder)
    fun onMinPriceChange(price: Double?)
    fun onMaxPriceChange(price: Double?)

    /** Commit all advanced-filter fields in one shot (single search) — used by the filter sheet. */
    fun onApplyFilters(
        minPrice: Double?,
        maxPrice: Double?,
        minRating: Double?,
        offersTelemedicine: Boolean?,
        offersHomeVisit: Boolean?,
        sortOrder: DoctorSortOrder,
    )

    /** Clear all advanced filters (price, rating, visit toggles, sort) while keeping query + specialty. */
    fun onResetFilters()

    fun onLoadMore()
    fun onDoctorTapped(doctorId: String)
    fun onErrorDismissed()

    sealed interface Output {
        data class NavigateToDoctorProfile(val doctorId: String) : Output
        data object Back : Output
    }
}

/**
 * @Serializable so Decompose [StateKeeper] can persist and restore this state
 * across Android configuration changes (orientation change, etc.).
 *
 * Note: [results] (the full doctor list) is intentionally excluded from
 * serialization by keeping only filter + pagination state. The results are
 * re-fetched on restore. This avoids bloating the saved state with a potentially
 * large list while still preserving user input (filters, query).
 *
 * REQ-4-009
 */
@Serializable
data class DoctorSearchState(
    val query: String = "",
    val selectedSpecialty: String? = null,
    val sortOrder: DoctorSortOrder = DoctorSortOrder.Recent,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minRating: Double? = null,
    val offersTelemedicine: Boolean? = null,
    val offersHomeVisit: Boolean? = null,
    @kotlinx.serialization.Transient val results: List<Doctor> = emptyList(),
    @kotlinx.serialization.Transient val isLoading: Boolean = false,
    val page: Int = 1,
    val hasMore: Boolean = true,
    @kotlinx.serialization.Transient val error: String? = null,
    /** Specialty catalog for the filter chips (id + name). Re-fetched on restore, not persisted. */
    @kotlinx.serialization.Transient val specialties: List<Specialty> = emptyList(),
) {
    /**
     * True when any advanced filter (or a non-default sort) is active.
     * Drives both the filter-icon badge dot and the distinct "no results with these
     * filters" empty-state copy on [com.inclinic.app.features.patient.presentation.ui.DoctorSearchScreen]
     * — pulled out of the screen so it's one pure, testable source of truth instead of
     * being recomputed inline.
     */
    val hasActiveFilters: Boolean
        get() = minPrice != null || maxPrice != null || minRating != null ||
            offersTelemedicine != null || offersHomeVisit != null || sortOrder != DoctorSortOrder.Recent
}
