package com.inclinic.app.features.doctor.patients.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.patients.core.model.PatientListItem
import com.inclinic.app.features.doctor.patients.core.model.PatientListStats
import com.inclinic.app.features.doctor.patients.core.model.PatientStatus

enum class PatientsFilter { ALL, FREE, ACTIVE, PREMIUM }

interface PatientsListComponent {
    val state: Value<PatientsListState>

    fun onRefresh()
    fun onFilterChange(filter: PatientsFilter)
    fun onPatientClicked(patientId: String)
    fun onSearchClicked()
    fun onBack()

    sealed interface Output {
        data class NavigateToPatient(val patientId: String) : Output
        data object NavigateToSearch : Output
        data object Back : Output
    }
}

data class PatientsListState(
    val patients: List<PatientListItem> = emptyList(),
    val stats: PatientListStats = PatientListStats(),
    val filter: PatientsFilter = PatientsFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    /** Pacientes ya filtrados por la pestaña seleccionada. */
    val visiblePatients: List<PatientListItem>
        get() = when (filter) {
            PatientsFilter.ALL -> patients
            PatientsFilter.FREE -> patients.filter { it.status == PatientStatus.ACTIVE }
            PatientsFilter.ACTIVE -> patients.filter {
                it.status == PatientStatus.ACTIVE || it.status == PatientStatus.PREMIUM
            }
            PatientsFilter.PREMIUM -> patients.filter { it.status == PatientStatus.PREMIUM }
        }
}
