package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultAppointmentDetailComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getAppointmentDetail: GetAppointmentDetailUseCase,
    private val getDoctorDetail: GetDoctorDetailUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AppointmentDetailComponent.Output) -> Unit,
) : AppointmentDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(AppointmentDetailState())
    override val state: Value<AppointmentDetailState> = _state

    init { load() }

    override fun onPayNow() { onOutput(AppointmentDetailComponent.Output.NavigateToPayment(appointmentId)) }

    override fun onCancel() { onOutput(AppointmentDetailComponent.Output.NavigateToCancel(appointmentId)) }

    override fun onReschedule() {
        val appt = _state.value.appointment ?: return
        val consultType = when (appt.visitType) {
            com.inclinic.app.core.model.VisitType.VIRTUAL -> "telemedicine"
            com.inclinic.app.core.model.VisitType.HOME -> "home"
            com.inclinic.app.core.model.VisitType.CLINIC -> "office"
        }
        onOutput(AppointmentDetailComponent.Output.NavigateToReschedule(appointmentId, appt.doctorId, consultType))
    }

    override fun onChat() {
        val s = _state.value
        val doctorId = s.appointment?.doctorId ?: s.doctor?.id ?: return
        val doctorName = s.doctor?.fullName ?: s.appointment?.doctorName ?: ""
        onOutput(AppointmentDetailComponent.Output.NavigateToChat(doctorId, doctorName))
    }

    override fun onBack() { onOutput(AppointmentDetailComponent.Output.Back) }

    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAppointmentDetail(appointmentId)
                .onSuccess { appt ->
                    _state.update { it.copy(isLoading = false, appointment = appt) }
                    getDoctorDetail(appt.doctorId)
                        .onSuccess { doctor -> _state.update { it.copy(doctor = doctor) } }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Failed to load appointment")) }
                }
        }
    }
}
