package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.features.patient.appointments.application.CancelAppointmentUseCase
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.time.Clock

class DefaultCancelAppointmentComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getAppointmentDetail: GetAppointmentDetailUseCase,
    private val cancelAppointment: CancelAppointmentUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (CancelAppointmentComponent.Output) -> Unit,
) : CancelAppointmentComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(CancelAppointmentState())
    override val state: Value<CancelAppointmentState> = _state

    init { load() }

    override fun onReasonChanged(reason: String) {
        _state.update { it.copy(reason = reason) }
    }

    override fun onConfirmCancel() {
        val appt = _state.value.appointment ?: return
        _state.update { it.copy(isCancelling = true, error = null) }
        scope.launch {
            val reason = _state.value.reason.ifBlank { "Sin motivo especificado" }
            cancelAppointment(appointmentId, appt.startsAt, reason)
                .onSuccess { onOutput(CancelAppointmentComponent.Output.Cancelled) }
                .onFailure { err ->
                    _state.update { it.copy(isCancelling = false, error = err.toUserMessage()) }
                }
        }
    }

    override fun onBack() { onOutput(CancelAppointmentComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAppointmentDetail(appointmentId)
                .onSuccess { appt ->
                    val now = Clock.System.now()
                    val days = (appt.startsAt - now).inWholeDays.toInt()
                    val canCancel = appt.status == AppointmentStatus.PENDING_PAYMENT || days >= 3
                    _state.update { it.copy(isLoading = false, appointment = appt, daysUntil = days, canCancel = canCancel) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage()) }
                }
        }
    }
}
