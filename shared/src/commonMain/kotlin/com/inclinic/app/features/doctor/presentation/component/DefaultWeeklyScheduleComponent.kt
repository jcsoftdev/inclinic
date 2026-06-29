package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.schedule.application.GetWeeklyScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class DefaultWeeklyScheduleComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getWeeklySchedule: GetWeeklyScheduleUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (WeeklyScheduleComponent.Output) -> Unit,
) : WeeklyScheduleComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(WeeklyScheduleState())
    override val state: Value<WeeklyScheduleState> = _state

    init {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val weekStart = today.startOfWeek()
        _state.update { it.copy(weekStart = weekStart) }
        load(weekStart)
    }

    override fun onPreviousWeek() {
        val current = _state.value.weekStart ?: return
        val prev = current.minus(7, DateTimeUnit.DAY)
        _state.update { it.copy(weekStart = prev) }
        load(prev)
    }

    override fun onNextWeek() {
        val current = _state.value.weekStart ?: return
        val next = current.plus(7, DateTimeUnit.DAY)
        _state.update { it.copy(weekStart = next) }
        load(next)
    }

    override fun onDayTap(date: LocalDate) {
        onOutput(WeeklyScheduleComponent.Output.NavigateToDailySchedule(date))
    }

    override fun onBack() { onOutput(WeeklyScheduleComponent.Output.Back) }

    private fun load(weekStart: LocalDate) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getWeeklySchedule(doctorId, weekStart.toString())
                .onSuccess { summaries ->
                    _state.update { it.copy(isLoading = false, daySummaries = summaries) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading week")) }
                }
        }
    }

    private fun LocalDate.startOfWeek(): LocalDate {
        var d = this
        while (d.dayOfWeek != DayOfWeek.MONDAY) d = d.minus(1, DateTimeUnit.DAY)
        return d
    }
}
