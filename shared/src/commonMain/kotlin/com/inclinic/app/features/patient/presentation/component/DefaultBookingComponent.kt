package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.VisitType
import com.inclinic.app.core.port.TelemetryService
import com.inclinic.app.features.patient.booking.application.CreateAppointmentUseCase
import com.inclinic.app.features.patient.doctor_profile.application.GetDoctorDetailUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultBookingComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val slotId: String,
    private val date: String,
    private val getDoctorDetail: GetDoctorDetailUseCase,
    private val createAppointment: CreateAppointmentUseCase,
    private val dispatchers: AppDispatchers,
    private val telemetry: TelemetryService? = null,
    private val onOutput: (BookingComponent.Output) -> Unit,
    consultType: String = "office",
    startTime: String = "",
) : BookingComponent, ComponentContext by componentContext {

    private val initialVisitType: VisitType = when (consultType) {
        "telemedicine" -> VisitType.VIRTUAL
        "home"         -> VisitType.HOME
        else           -> VisitType.CLINIC
    }

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    /**
     * Restore persisted volatile state (visitType, notes) across configuration changes.
     * The [doctor] field is transient and re-fetched every time.
     *
     * REQ-4-009
     */
    private val savedState: BookingState? =
        stateKeeper.consume("booking_state", BookingState.serializer())

    private val _state = MutableValue(
        (savedState ?: BookingState(slotId = slotId, date = date, startTime = startTime)).let {
            if (it.visitType == null) it.copy(visitType = initialVisitType) else it
        }
    )
    override val state: Value<BookingState> = _state

    init {
        stateKeeper.register("booking_state", BookingState.serializer()) { _state.value }
        scope.launch {
            getDoctorDetail(doctorId)
                .onSuccess { doctor -> _state.update { it.copy(doctor = doctor) } }
                .onFailure { err -> _state.update { it.copy(error = err.toUserMessage()) } }
        }
    }

    override fun onVisitTypeChange(visitType: VisitType) {
        _state.update { it.copy(visitType = visitType, visitTypeError = null) }
    }

    override fun onNotesChange(notes: String) { _state.update { it.copy(notes = notes) } }

    override fun onConfirm() {
        val s = _state.value
        if (s.visitType == null) {
            _state.update { it.copy(visitTypeError = "Please select a visit type") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            createAppointment(doctorId, s.date, s.slotId, s.visitType.name, s.notes.takeIf { it.isNotBlank() })
                .onSuccess { appointment ->
                    _state.update { it.copy(isLoading = false, isConfirmed = true) }
                    telemetry?.track("appointment_booked", mapOf("doctorId" to doctorId, "date" to date))
                    onOutput(BookingComponent.Output.NavigateToPayment(appointment.id))
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Booking failed")) }
                }
        }
    }

    override fun onSkipPayment() {
        val s = _state.value
        val visitType = s.visitType ?: initialVisitType
        _state.update { it.copy(isLoadingSkip = true, error = null) }
        scope.launch {
            createAppointment(doctorId, s.date, s.slotId, visitType.name, s.notes.takeIf { it.isNotBlank() })
                .onSuccess { onOutput(BookingComponent.Output.NavigateToAppointments) }
                .onFailure { err -> _state.update { it.copy(isLoadingSkip = false, error = err.toUserMessage("Error al agendar")) } }
        }
    }

    override fun onBack() { onOutput(BookingComponent.Output.Back) }
}
