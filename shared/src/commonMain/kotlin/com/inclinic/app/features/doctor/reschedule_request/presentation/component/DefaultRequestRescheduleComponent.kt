package com.inclinic.app.features.doctor.reschedule_request.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.appointments.application.GetDoctorAppointmentDetailUseCase
import com.inclinic.app.features.doctor.reschedule_request.application.GetDoctorAvailabilityUseCase
import com.inclinic.app.features.doctor.reschedule_request.application.RequestRescheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultRequestRescheduleComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getAppointmentDetail: GetDoctorAppointmentDetailUseCase,
    private val getAvailability: GetDoctorAvailabilityUseCase,
    private val requestReschedule: RequestRescheduleUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RequestRescheduleComponent.Output) -> Unit,
) : RequestRescheduleComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(RequestRescheduleState())
    override val state: Value<RequestRescheduleState> = _state

    init { load() }

    override fun onDateChange(date: String) {
        _state.update { it.copy(selectedDate = date, proposedSlot = "", availableSlots = emptyList(), error = null) }
        loadSlots(date)
    }

    override fun onSlotChange(value: String) {
        _state.update { it.copy(proposedSlot = value, error = null) }
    }

    private fun loadSlots(date: String) {
        val doctorId = _state.value.appointment?.doctorId ?: return
        _state.update { it.copy(isLoadingSlots = true) }
        scope.launch {
            getAvailability(doctorId, date)
                .onSuccess { slots ->
                    _state.update {
                        it.copy(
                            isLoadingSlots = false,
                            availableSlots = slots.filter { s -> s.isAvailable }.map { s -> s.startTime },
                        )
                    }
                }
                .onFailure { err -> _state.update { it.copy(isLoadingSlots = false, error = err.toUserMessage("Error cargando horarios")) } }
        }
    }

    override fun onMessageChange(value: String) {
        _state.update { it.copy(message = value) }
    }

    override fun onSubmit() {
        val s = _state.value
        if (!s.canSubmit) return
        _state.update { it.copy(isSubmitting = true, error = null) }
        scope.launch {
            requestReschedule(
                appointmentId = appointmentId,
                proposedSlot = s.proposedSlot.trim(),
                message = s.messageTrimmed,
            )
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    onOutput(RequestRescheduleComponent.Output.Success)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false, error = err.toUserMessage("Error requesting reschedule")) }
                }
        }
    }

    override fun onBack() {
        onOutput(RequestRescheduleComponent.Output.Back)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAppointmentDetail(appointmentId)
                .onSuccess { appt -> _state.update { it.copy(isLoading = false, appointment = appt) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading appointment")) } }
        }
    }
}
