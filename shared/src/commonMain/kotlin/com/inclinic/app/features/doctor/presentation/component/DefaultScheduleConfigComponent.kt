package com.inclinic.app.features.doctor.presentation.component

import com.inclinic.app.core.error.toUserMessage
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.config.application.GetScheduleConfigUseCase
import com.inclinic.app.features.doctor.config.application.SaveScheduleConfigUseCase
import com.inclinic.app.features.doctor.infrastructure.remote.DaySchedule
import com.inclinic.app.features.doctor.infrastructure.remote.WeeklySchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek

class DefaultScheduleConfigComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    private val getScheduleConfig: GetScheduleConfigUseCase,
    private val saveScheduleConfig: SaveScheduleConfigUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (ScheduleConfigComponent.Output) -> Unit,
) : ScheduleConfigComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(
        ScheduleConfigState(days = DayOfWeek.entries.map { DayScheduleUi(day = it) }),
    )
    override val state: Value<ScheduleConfigState> = _state

    init { load() }

    private fun mutateDay(day: DayOfWeek, transform: (DayScheduleUi) -> DayScheduleUi) {
        _state.update { st ->
            st.copy(
                days = st.days.map { if (it.day == day) transform(it) else it },
                saveSuccess = false,
            )
        }
    }

    override fun onToggleDay(day: DayOfWeek) = mutateDay(day) {
        it.copy(enabled = !it.enabled, expanded = !it.enabled)
    }

    override fun onExpandDay(day: DayOfWeek) = mutateDay(day) { it.copy(expanded = !it.expanded) }

    override fun onStartTimeChange(day: DayOfWeek, value: String) =
        mutateDay(day) { it.copy(startTime = value) }

    override fun onEndTimeChange(day: DayOfWeek, value: String) =
        mutateDay(day) { it.copy(endTime = value) }

    override fun onMaxPatientsChange(day: DayOfWeek, value: String) =
        mutateDay(day) { it.copy(maxPatients = value.filter(Char::isDigit)) }

    override fun onSlotDurationChange(day: DayOfWeek, minutes: Int) =
        mutateDay(day) { it.copy(slotDuration = minutes) }

    override fun onPriceChange(day: DayOfWeek, value: String) =
        mutateDay(day) { it.copy(price = value.filter { c -> c.isDigit() || c == '.' }) }

    override fun onToggleAllowNegotiation(day: DayOfWeek) =
        mutateDay(day) { it.copy(allowNegotiation = !it.allowNegotiation) }

    override fun onSave() {
        if (_state.value.isSaving) return
        val schedule = WeeklySchedule(
            days = _state.value.days.filter { it.enabled }.map { ui ->
                DaySchedule(
                    dayOfWeek = ui.day.name,
                    startTime = ui.startTime,
                    endTime = ui.endTime,
                    maxPatients = ui.maxPatients.toIntOrNull() ?: 8,
                    slotDuration = ui.slotDuration,
                    price = ui.price.toDoubleOrNull(),
                    allowVisitTypeNegotiation = ui.allowNegotiation,
                )
            },
        )
        _state.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
        scope.launch {
            saveScheduleConfig(doctorId, schedule)
                .onSuccess { _state.update { st -> st.copy(isSaving = false, saveSuccess = true) } }
                .onFailure { err ->
                    _state.update { st -> st.copy(isSaving = false, error = err.toUserMessage("Save failed")) }
                }
        }
    }

    override fun onBack() { onOutput(ScheduleConfigComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getScheduleConfig(doctorId)
                .onSuccess { weekly ->
                    val byDay = weekly.days.associateBy {
                        runCatching { DayOfWeek.valueOf(it.dayOfWeek) }.getOrNull()
                    }
                    val merged = DayOfWeek.entries.map { day ->
                        val block = byDay[day]
                        if (block == null) {
                            DayScheduleUi(day = day)
                        } else {
                            DayScheduleUi(
                                day = day,
                                enabled = true,
                                startTime = block.startTime,
                                endTime = block.endTime,
                                maxPatients = block.maxPatients.toString(),
                                slotDuration = block.slotDuration ?: 30,
                                price = block.price?.let { p ->
                                    if (p % 1.0 == 0.0) p.toInt().toString() else p.toString()
                                } ?: "",
                                allowNegotiation = block.allowVisitTypeNegotiation,
                            )
                        }
                    }
                    _state.update { it.copy(isLoading = false, days = merged) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Error loading schedule")) }
                }
        }
    }
}
