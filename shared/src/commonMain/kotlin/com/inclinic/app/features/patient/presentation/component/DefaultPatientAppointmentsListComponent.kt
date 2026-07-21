package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.Appointment
import com.inclinic.app.core.model.AppointmentStatus
import com.inclinic.app.features.patient.appointments.application.GetPatientAppointmentsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPatientAppointmentsListComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getAppointments: GetPatientAppointmentsUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PatientAppointmentsListComponent.Output) -> Unit,
) : PatientAppointmentsListComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PatientAppointmentsListState())
    override val state: Value<PatientAppointmentsListState> = _state

    init { load(page = 1, reset = true) }

    override fun onTabChange(tab: AppointmentsTab) {
        _state.update { it.copy(selectedTab = tab, page = 1) }
        load(page = 1, reset = true)
    }

    override fun onLoadMore() {
        val s = _state.value
        if (s.isLoading || !s.hasMore) return
        val nextPage = s.page + 1
        _state.update { it.copy(page = nextPage) }
        load(page = nextPage, reset = false)
    }

    override fun onRefresh() { load(page = 1, reset = true) }

    override fun onAppointmentTapped(appointmentId: String) {
        onOutput(PatientAppointmentsListComponent.Output.NavigateToAppointmentDetail(appointmentId))
    }

    override fun onPayNow(appointmentId: String) {
        onOutput(PatientAppointmentsListComponent.Output.NavigateToPayment(appointmentId))
    }

    override fun onCancel(appointmentId: String) {
        onOutput(PatientAppointmentsListComponent.Output.NavigateToCancel(appointmentId))
    }

    override fun onReschedule(appointmentId: String) {
        val appt = _state.value.appointments.find { it.id == appointmentId } ?: return
        val consultType = when (appt.visitType) {
            com.inclinic.app.core.model.VisitType.VIRTUAL -> "telemedicine"
            com.inclinic.app.core.model.VisitType.HOME -> "home"
            com.inclinic.app.core.model.VisitType.CLINIC -> "office"
        }
        onOutput(PatientAppointmentsListComponent.Output.NavigateToReschedule(appointmentId, appt.doctorId, consultType))
    }

    override fun onRespondReschedule(appointmentId: String) {
        onOutput(PatientAppointmentsListComponent.Output.NavigateToRescheduleResponse(appointmentId))
    }

    override fun onConfirmAttendance(appointmentId: String) {
        onOutput(PatientAppointmentsListComponent.Output.NavigateToConfirmRating(appointmentId))
    }

    override fun onReportProblem(appointmentId: String) {
        onOutput(PatientAppointmentsListComponent.Output.NavigateToDispute(appointmentId))
    }

    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    override fun onSearchDoctors() {
        onOutput(PatientAppointmentsListComponent.Output.NavigateToSearch)
    }

    private fun load(page: Int, reset: Boolean) {
        val tab = _state.value.selectedTab
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            // Backend rejects "upcoming"/"past" (only real AppointmentStatus values), so
            // fetch unfiltered and bucket by tab client-side via matchesTab.
            getAppointments(patientId, null, page)
                .onSuccess { appts ->
                    val filtered = appts.filter { matchesTab(it, tab) }
                    _state.update { it.copy(
                        isLoading = false,
                        appointments = if (reset) filtered else it.appointments + filtered,
                        hasMore = false,
                    ) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Failed to load appointments")) }
                }
        }
    }

    private fun matchesTab(appt: Appointment, tab: AppointmentsTab): Boolean = when (tab) {
        // PENDING_PAYMENT past its deadline is functionally dead (backend's cleanup-unpaid
        // cron deletes it, at latest within its 5-minute schedule) — don't show it as active
        // in the meantime, it would just confuse the patient.
        AppointmentsTab.ACTIVE -> appt.status in ACTIVE_STATUSES && !isExpiredPendingPayment(appt) && !appt.needsClosure
        AppointmentsTab.NEEDS_CLOSURE -> appt.needsClosure
        AppointmentsTab.COMPLETED -> appt.status in COMPLETED_STATUSES
        AppointmentsTab.CANCELLED -> appt.status in CANCELLED_STATUSES || isExpiredPendingPayment(appt)
    }

    private fun isExpiredPendingPayment(appt: Appointment): Boolean =
        appt.status == AppointmentStatus.PENDING_PAYMENT &&
            appt.paymentDeadline != null &&
            appt.paymentDeadline <= kotlin.time.Clock.System.now()

    private companion object {
        val ACTIVE_STATUSES = setOf(
            AppointmentStatus.PENDING_PAYMENT,
            AppointmentStatus.SCHEDULED,
            AppointmentStatus.CONFIRMED,
            AppointmentStatus.IN_PROGRESS,
        )
        val COMPLETED_STATUSES = setOf(
            AppointmentStatus.COMPLETED,
            AppointmentStatus.DISPUTED,
            AppointmentStatus.REFUNDED,
        )
        val CANCELLED_STATUSES = setOf(
            AppointmentStatus.CANCELLED_BY_PATIENT,
            AppointmentStatus.CANCELLED_BY_DOCTOR,
            AppointmentStatus.NO_SHOW,
        )
    }
}
