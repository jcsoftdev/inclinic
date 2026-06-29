package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.doctors.application.GetAdminDoctorsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminDoctorsComponent(
    componentContext: ComponentContext,
    private val getDoctors: GetAdminDoctorsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminDoctorsComponent.Output) -> Unit,
) : AdminDoctorsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminDoctorsState())
    override val state: Value<AdminDoctorsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    override fun onFilterChange(filter: AdminDoctorsFilter) {
        if (filter == AdminDoctorsFilter.Pending) {
            onOutput(AdminDoctorsComponent.Output.NavigateToPendingApprovals)
            return
        }
        _state.update { it.copy(activeFilter = filter) }
        // Re-fetch with server-side status filter when switching to ACTIVE or SUSPENDED
        load(filter)
    }

    override fun onDoctorClicked(doctorId: String) {
        onOutput(AdminDoctorsComponent.Output.NavigateToDetail(doctorId))
    }

    override fun onNavigateToPendingApprovals() {
        onOutput(AdminDoctorsComponent.Output.NavigateToPendingApprovals)
    }

    private fun load(filter: AdminDoctorsFilter = _state.value.activeFilter) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            val apiStatus = filter.apiStatus // null = ALL from server, client filters Pending chip separately
            getDoctors(status = apiStatus)
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, allItems = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando doctores")) }
                }
        }
    }
}
