package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.infrastructure.remote.AdminPatientListItem
import com.inclinic.app.features.admin.patients.application.UnsuspendUserUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminPatientDetailComponent(
    componentContext: ComponentContext,
    patient: AdminPatientListItem,
    private val unsuspendUser: UnsuspendUserUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminPatientDetailComponent.Output) -> Unit,
) : AdminPatientDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminPatientDetailState(patient = patient))
    override val state: Value<AdminPatientDetailState> = _state

    override fun onBack() {
        onOutput(AdminPatientDetailComponent.Output.Back)
    }

    override fun onSuspend() {
        onOutput(AdminPatientDetailComponent.Output.NavigateToSuspend(_state.value.patient))
    }

    override fun onViewAppointments() {
        onOutput(AdminPatientDetailComponent.Output.NavigateToAppointments(_state.value.patient.id))
    }

    override fun onReactivate() {
        val patient = _state.value.patient
        _state.update { it.copy(isReactivating = true, reactivateError = null) }
        scope.launch {
            unsuspendUser(patient.userId)
                .onSuccess {
                    onOutput(AdminPatientDetailComponent.Output.ReactivateSuccess)
                }
                .onFailure { err ->
                    _state.update { it.copy(isReactivating = false, reactivateError = err.toUserMessage("Error reactivando usuario")) }
                }
        }
    }
}
