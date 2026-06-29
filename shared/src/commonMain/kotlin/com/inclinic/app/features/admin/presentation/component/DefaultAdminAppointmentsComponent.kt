package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.appointments.application.GetAdminAppointmentsUseCase
import com.inclinic.app.features.admin.infrastructure.remote.AdminAppointmentFilters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminAppointmentsComponent(
    componentContext: ComponentContext,
    private val getAppointments: GetAdminAppointmentsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminAppointmentsComponent.Output) -> Unit,
) : AdminAppointmentsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminAppointmentsState())
    override val state: Value<AdminAppointmentsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    override fun onFilterChange(filter: AdminAppointmentsFilter) {
        _state.update { it.copy(activeFilter = filter) }
    }

    override fun onAppointmentClicked(appointmentId: String) {
        onOutput(AdminAppointmentsComponent.Output.NavigateToDetail(appointmentId))
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            // Load all items; client-side filters (Today, Held) applied in state.visibleItems.
            // CANCELLED is also client-side filtered since we fetch everything (cap 200 on backend).
            getAppointments(AdminAppointmentFilters())
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, allItems = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando citas")) }
                }
        }
    }
}
