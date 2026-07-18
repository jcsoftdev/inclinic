package com.inclinic.app.features.admin.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.admin.appointments.application.GetAdminAppointmentDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAdminAppointmentDetailComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getDetail: GetAdminAppointmentDetailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AdminAppointmentDetailComponent.Output) -> Unit,
) : AdminAppointmentDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AdminAppointmentDetailState())
    override val state: Value<AdminAppointmentDetailState> = _state

    init { load() }

    override fun onBack() {
        onOutput(AdminAppointmentDetailComponent.Output.Back)
    }

    override fun onNavigateToResolveDispute() {
        // Guard: the screen already hides this action when there's no dispute, but the
        // component re-checks so no output can fire from a stale/mismatched UI state.
        val detail = _state.value.detail ?: return
        if (!detail.hasDispute) return
        onOutput(AdminAppointmentDetailComponent.Output.NavigateToResolveDispute(appointmentId))
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getDetail(appointmentId)
                .onSuccess { detail ->
                    _state.update { it.copy(isLoading = false, detail = detail) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error cargando cita")) }
                }
        }
    }
}
