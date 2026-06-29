package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.patient.home.application.GetPatientDashboardUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DefaultPatientHomeComponent(
    componentContext: ComponentContext,
    private val patientId: String,
    private val getDashboard: GetPatientDashboardUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (PatientHomeComponent.Output) -> Unit,
) : PatientHomeComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(PatientHomeState())
    override val state: Value<PatientHomeState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onSearchTapped() { onOutput(PatientHomeComponent.Output.NavigateToSearch) }

    override fun onDoctorTapped(doctorId: String) {
        onOutput(PatientHomeComponent.Output.NavigateToDoctorProfile(doctorId))
    }

    override fun onAssistantChatTapped() {
        onOutput(PatientHomeComponent.Output.NavigateToAssistantChat)
    }

    override fun onAppointmentsTapped() {
        onOutput(PatientHomeComponent.Output.NavigateToAppointments)
    }

    override fun onAppointmentDetailTapped(appointmentId: String) {
        onOutput(PatientHomeComponent.Output.NavigateToAppointmentDetail(appointmentId))
    }

    override fun onProfileTapped() {
        onOutput(PatientHomeComponent.Output.NavigateToProfile)
    }

    override fun onPackagesTapped() {
        onOutput(PatientHomeComponent.Output.NavigateToPackages)
    }

    override fun onPremiumTapped() {
        onOutput(PatientHomeComponent.Output.NavigateToPremium)
    }

    override fun onNavigateToHistoryAccess() {
        onOutput(PatientHomeComponent.Output.NavigateToHistoryAccess)
    }

    override fun onErrorDismissed() { _state.update { it.copy(error = null) } }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getDashboard(patientId)
                .onSuccess { dashboard ->
                    _state.update { it.copy(
                        isLoading = false,
                        upcomingCount = dashboard.upcomingCount,
                        recentDoctors = dashboard.recentDoctors,
                        nextAppointment = dashboard.nextAppointment,
                    ) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Failed to load dashboard")) }
                }
        }
    }
}
