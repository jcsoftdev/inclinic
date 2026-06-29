package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import com.inclinic.app.features.admin.patients.application.GetPatientsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminPatientsComponent(
    componentContext: ComponentContext,
    private val getPatients: GetPatientsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminPatientsComponent.Output) -> Unit,
) : AdminPatientsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminPatientsState())
    override val state: Value<AdminPatientsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    override fun onFilterChange(filter: AdminPatientsFilter) {
        _state.update { it.copy(activeFilter = filter) }
        // Suspended chip has a server-side status filter; others use client-side filtering
        if (filter == AdminPatientsFilter.Suspended) load(filter)
        else if (_state.value.allItems.isEmpty()) load(filter)
    }

    override fun onPatientClicked(patient: AdminPatientListItem) {
        onOutput(AdminPatientsComponent.Output.NavigateToDetail(patient))
    }

    override fun onBack() {
        onOutput(AdminPatientsComponent.Output.Back)
    }

    private fun load(filter: AdminPatientsFilter = _state.value.activeFilter) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val apiStatus = filter.apiStatus
            getPatients(status = apiStatus)
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, allItems = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando pacientes")) }
                }
        }
    }
}
