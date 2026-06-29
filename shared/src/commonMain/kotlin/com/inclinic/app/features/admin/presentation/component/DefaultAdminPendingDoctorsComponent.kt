package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.doctors.application.GetPendingDoctorsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminPendingDoctorsComponent(
    componentContext: ComponentContext,
    private val getPendingDoctors: GetPendingDoctorsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminPendingDoctorsComponent.Output) -> Unit,
) : AdminPendingDoctorsComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminPendingDoctorsState())
    override val state: Value<AdminPendingDoctorsState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onDoctorClicked(doctorId: String) {
        onOutput(AdminPendingDoctorsComponent.Output.NavigateToPendingDetail(doctorId))
    }

    override fun onBack() {
        onOutput(AdminPendingDoctorsComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getPendingDoctors()
                .onSuccess { items ->
                    _state.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando solicitudes")) }
                }
        }
    }
}
