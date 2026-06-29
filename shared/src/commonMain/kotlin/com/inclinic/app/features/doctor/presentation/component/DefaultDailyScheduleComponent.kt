package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.schedule.application.GetDailyScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class DefaultDailyScheduleComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getDailySchedule: GetDailyScheduleUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (DailyScheduleComponent.Output) -> Unit,
) : DailyScheduleComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(DailyScheduleState())
    override val state: Value<DailyScheduleState> = _state

    init {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        _state.update { it.copy(date = today) }
        load(today)
    }

    override fun onPreviousDay() {
        val current = _state.value.date ?: return
        val prev = current.plus(-1, DateTimeUnit.DAY)
        _state.update { it.copy(date = prev) }
        load(prev)
    }

    override fun onNextDay() {
        val current = _state.value.date ?: return
        val next = current.plus(1, DateTimeUnit.DAY)
        _state.update { it.copy(date = next) }
        load(next)
    }

    override fun onAppointmentTap(appointmentId: String) {
        onOutput(DailyScheduleComponent.Output.NavigateToAppointmentDetail(appointmentId))
    }

    override fun onOpenRescheduleQueue() {
        onOutput(DailyScheduleComponent.Output.OpenRescheduleQueue)
    }

    override fun onBack() { onOutput(DailyScheduleComponent.Output.Back) }

    private fun load(date: LocalDate) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getDailySchedule(doctorId, date.toString())
                .onSuccess { appts ->
                    _state.update { it.copy(isLoading = false, appointments = appts.sortedBy { a -> a.startsAt }) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading schedule")) }
                }
        }
    }
}
