package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.DisputeReason
import com.inclinic.app.features.patient.appointments.application.DisputeAppointmentUseCase
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultDisputeAppointmentComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getAppointmentDetail: GetAppointmentDetailUseCase,
    private val disputeAppointment: DisputeAppointmentUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DisputeAppointmentComponent.Output) -> Unit,
) : DisputeAppointmentComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DisputeAppointmentState())
    override val state: Value<DisputeAppointmentState> = _state

    init { load() }

    override fun onReasonSelected(reason: DisputeReason) {
        _state.update { it.copy(selectedReason = reason) }
    }

    override fun onDetailsChanged(details: String) {
        _state.update { it.copy(details = details.take(300)) }
    }

    override fun onSubmit() {
        val reason = _state.value.selectedReason ?: return
        val details = _state.value.details.ifBlank { return }
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            disputeAppointment(appointmentId, reason.name, details)
                .onSuccess { onOutput(DisputeAppointmentComponent.Output.Disputed) }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage()) }
                }
        }
    }

    override fun onBack() { onOutput(DisputeAppointmentComponent.Output.Back) }

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
