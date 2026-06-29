package com.inclinic.app.features.doctor.patients.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.patients.application.GetDoctorPatientsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPatientsListComponent(
    componentContext: ComponentContext,
    private val getPatients: GetDoctorPatientsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PatientsListComponent.Output) -> Unit,
) : PatientsListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PatientsListState())
    override val state: Value<PatientsListState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onFilterChange(filter: PatientsFilter) {
        _state.update { it.copy(filter = filter) }
    }

    override fun onPatientClicked(patientId: String) {
        onOutput(PatientsListComponent.Output.NavigateToPatient(patientId))
    }

    override fun onSearchClicked() {
        onOutput(PatientsListComponent.Output.NavigateToSearch)
    }

    override fun onBack() {
        onOutput(PatientsListComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPatients()
                .onSuccess { list -> _state.update { it.copy(isLoading = false, patients = list.items, stats = list.stats) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading patients")) } }
        }
    }
}
