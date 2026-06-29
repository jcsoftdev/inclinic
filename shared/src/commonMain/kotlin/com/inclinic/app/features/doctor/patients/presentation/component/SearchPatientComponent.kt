package com.inclinic.app.features.doctor.patients.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem

interface SearchPatientComponent {
    val state: Value<SearchPatientState>

    fun onQueryChange(query: String)
    fun onSearch()
    fun onPatientClicked(patientId: String)
    fun onBack()

    sealed interface Output {
        data class NavigateToPatient(val patientId: String) : Output
        data object Back : Output
    }
}

data class SearchPatientState(
    val query: String = "",
    val results: List<PatientListItem> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false,
)
