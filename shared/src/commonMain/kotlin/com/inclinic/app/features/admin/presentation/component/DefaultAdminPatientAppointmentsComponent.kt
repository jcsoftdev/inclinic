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

class DefaultAdminPatientAppointmentsComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getAppointments: GetAdminAppointmentsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminPatientAppointmentsComponent.Output) -> Unit,
) : AdminPatientAppointmentsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminPatientAppointmentsState())
    override val state: Value<AdminPatientAppointmentsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onBack() {
        onOutput(AdminPatientAppointmentsComponent.Output.Back)
    }

    override fun onAppointmentClicked(appointmentId: String) {
        onOutput(AdminPatientAppointmentsComponent.Output.NavigateToDetail(appointmentId))
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAppointments(AdminAppointmentFilters(patientId = patientId))
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, appointments = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando citas")) }
                }
        }
    }
}
