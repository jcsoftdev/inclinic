package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.appointments.application.RequestVisitTypeChangeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultChangeVisitTypeComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getAppointmentDetail: GetAppointmentDetailUseCase,
    private val requestVisitTypeChange: RequestVisitTypeChangeUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ChangeVisitTypeComponent.Output) -> Unit,
) : ChangeVisitTypeComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(ChangeVisitTypeState())
    override val state: Value<ChangeVisitTypeState> = _state

    init { load() }

    override fun onNewVisitTypeSelected(type: VisitType) {
        _state.update { it.copy(newVisitType = type) }
    }

    override fun onAddressChanged(address: String) {
        _state.update { it.copy(address = address) }
    }

    override fun onReasonChanged(reason: String) {
        _state.update { it.copy(reason = reason) }
    }

    override fun onSubmit() {
        val newType = _state.value.newVisitType ?: return
        val address = _state.value.address.ifBlank { null }
        val reason = _state.value.reason.ifBlank { null }
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            requestVisitTypeChange(appointmentId, newType.name, reason, address)
                .onSuccess { onOutput(ChangeVisitTypeComponent.Output.Requested) }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage()) }
                }
        }
    }

    override fun onBack() { onOutput(ChangeVisitTypeComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAppointmentDetail(appointmentId)
                .onSuccess { appt ->
                    _state.update { it.copy(isLoading = false, appointment = appt) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage()) }
                }
        }
    }
}
