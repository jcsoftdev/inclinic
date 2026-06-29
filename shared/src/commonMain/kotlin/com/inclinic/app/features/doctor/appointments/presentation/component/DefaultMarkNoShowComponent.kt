package com.inclinic.app.features.doctor.appointments.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.appointments.application.GetDoctorAppointmentDetailUseCase
import com.inclinic.app.features.doctor.appointments.application.NoShowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultMarkNoShowComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getAppointmentDetail: GetDoctorAppointmentDetailUseCase,
    private val noShowUseCase: NoShowUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (MarkNoShowComponent.Output) -> Unit,
) : MarkNoShowComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(MarkNoShowState())
    override val state: Value<MarkNoShowState> = _state

    init { load() }

    override fun onReasonChanged(value: String) {
        _state.update { it.copy(reason = value.take(MarkNoShowState.MAX_REASON_LENGTH)) }
    }

    override fun onConfirm() {
        val appt = _state.value.appointment ?: return
        if (_state.value.isSubmitting || !_state.value.canConfirm) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            noShowUseCase(appt)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onOutput(MarkNoShowComponent.Output.Success)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("No-show failed")) }
                }
        }
    }

    override fun onBack() { onOutput(MarkNoShowComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAppointmentDetail(appointmentId)
                .onSuccess { appt -> _state.update { it.copy(isLoading = false, appointment = appt) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading appointment")) } }
        }
    }
}
