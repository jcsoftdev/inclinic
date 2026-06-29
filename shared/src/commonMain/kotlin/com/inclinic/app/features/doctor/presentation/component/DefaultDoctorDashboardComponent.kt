package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.util.formatThousands
import com.inclinic.app.features.doctor.dashboard.application.GetDoctorDashboardUseCase
import com.inclinic.app.features.doctor.schedule.application.GetDailyScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DefaultDoctorDashboardComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getDashboard: GetDoctorDashboardUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DoctorDashboardComponent.Output) -> Unit,
    private val getDailySchedule: GetDailyScheduleUseCase? = null,
) : DoctorDashboardComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DoctorDashboardState())
    override val state: Value<DoctorDashboardState> = _state

    init { load() }

    override fun onRefresh() { load() }

    override fun onNavigateToSchedule() {
        onOutput(DoctorDashboardComponent.Output.NavigateToSchedule)
    }

    override fun onNavigateToPendingAppointments() {
        onOutput(DoctorDashboardComponent.Output.NavigateToPendingAppointments)
    }

    override fun onNavigateToNotifications() {
        onOutput(DoctorDashboardComponent.Output.NavigateToNotifications)
    }

    override fun onAppointmentTap(appointmentId: String) {
        onOutput(DoctorDashboardComponent.Output.NavigateToAppointmentDetail(appointmentId))
    }

    override fun onCreateMedicalRecord() {
        onOutput(DoctorDashboardComponent.Output.NavigateToCreateMedicalRecord)
    }

    override fun onNavigateToPackages() {
        onOutput(DoctorDashboardComponent.Output.NavigateToPackages)
    }

    override fun onNavigateToPatients() {
        onOutput(DoctorDashboardComponent.Output.NavigateToPatients)
    }

    override fun onNavigateToIncome() {
        onOutput(DoctorDashboardComponent.Output.NavigateToIncome)
    }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getDashboard(doctorId)
                .onSuccess { data ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            todayCount = data.todayCount,
                            pendingCount = data.pendingCount,
                            monthlyEarnings = "S/${data.monthlyEarnings.roundToLong().formatThousands()}",
                            ratingAverage = data.ratingAverage,
                            ratingCount = data.ratingCount,
                            completedCount = data.completedCount,
                            completedThisMonth = data.completedThisMonth,
                            patientsCount = data.patientsCount,
                            recurringPatientsCount = data.recurringPatientsCount,
                            completedTodayPct = data.completedTodayPct,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading dashboard")) }
                }
        }
        loadUpcoming()
    }

    private fun loadUpcoming() {
        val useCase = getDailySchedule ?: return
        scope.launch {
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            useCase(doctorId, today.toString())
                .onSuccess { appts ->
                    _state.update {
                        it.copy(upcomingAppointments = appts.sortedBy { a -> a.startsAt }.take(3))
                    }
                }
        }
    }
}
