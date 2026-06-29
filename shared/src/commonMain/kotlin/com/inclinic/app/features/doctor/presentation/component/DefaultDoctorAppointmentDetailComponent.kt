package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.appointments.application.CompleteAppointmentUseCase
import com.inclinic.app.features.doctor.appointments.application.ConfirmAppointmentUseCase
import com.inclinic.app.features.doctor.appointments.application.GetDoctorAppointmentDetailUseCase
import com.inclinic.app.features.doctor.appointments.application.NoShowUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultDoctorAppointmentDetailComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getDetail: GetDoctorAppointmentDetailUseCase,
    private val confirmAppointment: ConfirmAppointmentUseCase,
    private val completeAppointment: CompleteAppointmentUseCase,
    private val noShowUseCase: NoShowUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DoctorAppointmentDetailComponent.Output) -> Unit,
) : DoctorAppointmentDetailComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DoctorAppointmentDetailState())
    override val state: Value<DoctorAppointmentDetailState> = _state

    init { load() }

    override fun onConfirm() {
        val appt = _state.value.appointment ?: return
        if (_state.value.actionInProgress) return
        _state.update { it.copy(actionInProgress = true, error = null) }
        scope.launch {
            confirmAppointment(appt)
                .onSuccess { updated -> _state.update { it.copy(actionInProgress = false, appointment = updated) } }
                .onFailure { err -> _state.update { it.copy(actionInProgress = false, error = err.toUserMessage("Confirm failed")) } }
        }
    }

    override fun onComplete(selectedPhotos: List<ByteArray>) {
        val appt = _state.value.appointment ?: return
        if (_state.value.actionInProgress) return
        _state.update { it.copy(actionInProgress = true, error = null) }
        scope.launch {
            completeAppointment(appt, selectedPhotos)
                .onSuccess { updated -> _state.update { it.copy(actionInProgress = false, appointment = updated) } }
                .onFailure { err -> _state.update { it.copy(actionInProgress = false, error = err.toUserMessage("Complete failed")) } }
        }
    }

    override fun onNoShow() { _state.update { it.copy(showNoShowDialog = true) } }

    override fun onNoShowConfirmed() {
        val appt = _state.value.appointment ?: return
        _state.update { it.copy(showNoShowDialog = false, actionInProgress = true, error = null) }
        scope.launch {
            noShowUseCase(appt)
                .onSuccess { updated -> _state.update { it.copy(actionInProgress = false, appointment = updated) } }
                .onFailure { err -> _state.update { it.copy(actionInProgress = false, error = err.toUserMessage("No-show failed")) } }
        }
    }

    override fun onNoShowDismissed() { _state.update { it.copy(showNoShowDialog = false) } }

    override fun onNavigateToPatient() {
        val patientId = _state.value.appointment?.patientId ?: return
        onOutput(DoctorAppointmentDetailComponent.Output.NavigateToPatientDetail(patientId))
    }

    override fun onNavigateToChat() {
        onOutput(DoctorAppointmentDetailComponent.Output.NavigateToChat(appointmentId))
    }

    override fun onRequestReschedule() {
        onOutput(DoctorAppointmentDetailComponent.Output.NavigateToRequestReschedule(appointmentId))
    }

    override fun onCreateMedicalRecord() {
        val patientId = _state.value.appointment?.patientId ?: return
        onOutput(
            DoctorAppointmentDetailComponent.Output.NavigateToCreateMedicalRecord(
                appointmentId = appointmentId,
                patientId = patientId,
            ),
        )
    }

    override fun onBack() { onOutput(DoctorAppointmentDetailComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getDetail(appointmentId)
                .onSuccess { appt -> _state.update { it.copy(isLoading = false, appointment = appt) } }
                .onFailure { err -> _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading appointment")) } }
        }
    }
}
