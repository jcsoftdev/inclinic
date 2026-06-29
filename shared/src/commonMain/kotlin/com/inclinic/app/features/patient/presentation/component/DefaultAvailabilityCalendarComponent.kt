package com.inclinic.app.features.patient.presentation.component

import com.inclinic.app.core.error.toUserMessage
import kotlinx.datetime.number

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.core.model.AvailabilitySlot
import com.inclinic.app.features.patient.availability.application.GetAvailabilityUseCase
import com.inclinic.app.features.patient.availability.application.GetMonthAvailabilityUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class DefaultAvailabilityCalendarComponent(
    componentContext: ComponentContext,
    private val doctorId: String,
    @Suppress("UNUSED_PARAMETER") consultType: String = "office",
    private val getAvailability: GetAvailabilityUseCase,
    private val getMonthAvailability: GetMonthAvailabilityUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (AvailabilityCalendarComponent.Output) -> Unit,
) : AvailabilityCalendarComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val currentMonthStart = LocalDate(today.year, today.month.number, 1)

    private val _state = MutableValue(
        AvailabilityCalendarState(
            doctorId = doctorId,
            displayMonth = currentMonthStart,
            selectedDate = today,
        )
    )
    override val state: Value<AvailabilityCalendarState> = _state

    init {
        loadSlots(today)
        loadMonthLevels(currentMonthStart)
    }

    override fun onDateSelected(date: LocalDate) {
        if (date < today) return
        _state.update { it.copy(selectedDate = date, selectedSlot = null) }
        loadSlots(date)
    }

    override fun onSlotSelected(slot: AvailabilitySlot) {
        if (!slot.isAvailable) return
        _state.update { it.copy(selectedSlot = slot) }
    }

    override fun onPrevMonth() {
        val current = _state.value.displayMonth
        val prev = if (current.month.number == 1) {
            LocalDate(current.year - 1, 12, 1)
        } else {
            LocalDate(current.year, current.month.number - 1, 1)
        }
        if (prev >= currentMonthStart) {
            _state.update { it.copy(displayMonth = prev) }
            loadMonthLevels(prev)
        }
    }

    override fun onNextMonth() {
        val current = _state.value.displayMonth
        val next = if (current.month.number == 12) {
            LocalDate(current.year + 1, 1, 1)
        } else {
            LocalDate(current.year, current.month.number + 1, 1)
        }
        _state.update { it.copy(displayMonth = next) }
        loadMonthLevels(next)
    }

    override fun onContinue() {
        val s = _state.value
        val slot = s.selectedSlot ?: return
        val date = s.selectedDate ?: return
        onOutput(AvailabilityCalendarComponent.Output.NavigateToBooking(doctorId, slot.id, date.toString(), slot.startTime))
    }

    override fun onBack() { onOutput(AvailabilityCalendarComponent.Output.Back) }

    private fun loadSlots(date: LocalDate) {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAvailability(doctorId, date.toString())
                .onSuccess { slots ->
                    _state.update { it.copy(isLoading = false, slots = slots) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage("Failed to load availability")) }
                }
        }
    }

    private fun loadMonthLevels(monthStart: LocalDate) {
        val month = "${monthStart.year}-${monthStart.month.number.toString().padStart(2, '0')}"
        _state.update { it.copy(isLoadingMonth = true) }
        scope.launch {
            getMonthAvailability(doctorId, month)
                .onSuccess { rawMap ->
                    val parsed = rawMap.entries.mapNotNull { (dateStr, level) ->
                        runCatching { LocalDate.parse(dateStr) to levelFromString(level) }.getOrNull()
                    }.toMap()
                    _state.update { it.copy(isLoadingMonth = false, dayLevels = parsed) }
                }
                .onFailure {
                    _state.update { it.copy(isLoadingMonth = false) }
                }
        }
    }

    private fun levelFromString(s: String): DayLevel = when (s) {
        "open" -> DayLevel.OPEN
        "few" -> DayLevel.FEW
        else -> DayLevel.NONE
    }
}
