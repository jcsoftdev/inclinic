package com.inclinic.app.features.patient.presentation.component

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DoctorSearchStateHasActiveFiltersTest {

    @Test
    fun defaults_have_no_active_filters() {
        assertFalse(DoctorSearchState().hasActiveFilters)
    }

    @Test
    fun minPrice_alone_counts_as_active() {
        assertTrue(DoctorSearchState(minPrice = 50.0).hasActiveFilters)
    }

    @Test
    fun maxPrice_alone_counts_as_active() {
        assertTrue(DoctorSearchState(maxPrice = 200.0).hasActiveFilters)
    }

    @Test
    fun minRating_alone_counts_as_active() {
        assertTrue(DoctorSearchState(minRating = 4.0).hasActiveFilters)
    }

    @Test
    fun telemedicine_toggle_counts_as_active() {
        assertTrue(DoctorSearchState(offersTelemedicine = true).hasActiveFilters)
    }

    @Test
    fun home_visit_toggle_counts_as_active() {
        assertTrue(DoctorSearchState(offersHomeVisit = true).hasActiveFilters)
    }

    @Test
    fun non_default_sort_counts_as_active() {
        assertTrue(DoctorSearchState(sortOrder = DoctorSortOrder.TopRated).hasActiveFilters)
    }

    @Test
    fun query_and_specialty_alone_do_not_count_as_active_filters() {
        // Free-text query and the specialty chip row have their own "no results" copy path —
        // only the advanced-filter-sheet fields (+ non-default sort) drive hasActiveFilters.
        assertFalse(DoctorSearchState(query = "Ana", selectedSpecialty = "spec-1").hasActiveFilters)
    }
}
