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
import com.inclinic.app.features.patient.appointments.application.GetAppointmentDetailUseCase
import com.inclinic.app.features.patient.appointments.application.RescheduleAppointmentUseCase
import com.inclinic.app.features.patient.availability.application.GetAvailabilityUseCase
import com.inclinic.app.features.patient.availability.application.GetMonthAvailabilityUseCase
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class DefaultRescheduleAppointmentComponent(
    componentContext: ComponentContext,
    private val appointmentId: String,
    private val getAppointmentDetail: GetAppointmentDetailUseCase,
    private val rescheduleAppointment: RescheduleAppointmentUseCase,
    private val getAvailability: GetAvailabilityUseCase,
    private val getMonthAvailability: GetMonthAvailabilityUseCase,
    private val dispatchers: AppDispatchers,
    private val onOutput: (RescheduleAppointmentComponent.Output) -> Unit,
) : RescheduleAppointmentComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())
    init { lifecycle.doOnDestroy { scope.cancel() } }

    private val _state = MutableValue(
        RescheduleAppointmentState(
            displayMonth = Clock.System.todayIn(TimeZone.currentSystemDefault()).let {
                LocalDate(it.year, it.month.number, 1)
            }
        )
    )
    override val state: Value<RescheduleAppointmentState> = _state

    init { load() }

    override fun onDateSelected(date: LocalDate) {
        _state.update { it.copy(selectedDate = date, selectedSlot = null, slots = emptyList()) }
        loadSlots(date)
    }

    override fun onSlotSelected(slot: AvailabilitySlot) {
        _state.update { it.copy(selectedSlot = slot) }
    }

    override fun onPrevMonth() {
        val current = _state.value.displayMonth
        val prev = if (current.month.number == 1) {
            LocalDate(current.year - 1, 12, 1)
        } else {
            LocalDate(current.year, current.month.number - 1, 1)
        }
        _state.update { it.copy(displayMonth = prev) }
        loadMonthAvailability(prev)
    }

    override fun onNextMonth() {
        val current = _state.value.displayMonth
        val next = if (current.month.number == 12) {
            LocalDate(current.year + 1, 1, 1)
        } else {
            LocalDate(current.year, current.month.number + 1, 1)
        }
        _state.update { it.copy(displayMonth = next) }
        loadMonthAvailability(next)
    }

    override fun onConfirmReschedule() {
        val slot = _state.value.selectedSlot ?: return
        val date = _state.value.selectedDate ?: return
        _state.update { it.copy(isRescheduling = true, error = null) }
        scope.launch {
            rescheduleAppointment(appointmentId, date.toString(), slot.id)
                .onSuccess { onOutput(RescheduleAppointmentComponent.Output.Rescheduled) }
                .onFailure { err ->
                    _state.update { it.copy(isRescheduling = false, error = err.toUserMessage()) }
                }
        }
    }

    override fun onBack() { onOutput(RescheduleAppointmentComponent.Output.Back) }

    private fun load() {
        _state.update { it.copy(isLoading = true, error = null) }
        scope.launch {
            getAppointmentDetail(appointmentId)
                .onSuccess { appt ->
                    _state.update { it.copy(isLoading = false, appointment = appt) }
                    loadMonthAvailability(_state.value.displayMonth)
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, error = err.toUserMessage()) }
                }
        }
    }

    private fun loadMonthAvailability(month: LocalDate) {
        val doctorId = _state.value.appointment?.doctorId ?: return
        val monthStr = "${month.year}-${month.month.number.toString().padStart(2, '0')}"
        _state.update { it.copy(isLoadingMonth = true) }
        scope.launch {
            getMonthAvailability(doctorId, monthStr)
                .onSuccess { levels ->
                    val parsed = levels.mapNotNull { (dateStr, level) ->
                        val localDate = runCatching { LocalDate.parse(dateStr) }.getOrNull()
                        val dayLevel = when (level) {
                            "open" -> DayLevel.OPEN
                            "few" -> DayLevel.FEW
                            else -> DayLevel.NONE
                        }
                        localDate?.let { it to dayLevel }
                    }.toMap()
                    _state.update { it.copy(isLoadingMonth = false, dayLevels = parsed) }
                }
                .onFailure { _state.update { it.copy(isLoadingMonth = false) } }
        }
    }

    private fun loadSlots(date: LocalDate) {
        val doctorId = _state.value.appointment?.doctorId ?: return
        _state.update { it.copy(isLoadingSlots = true) }
        scope.launch {
            getAvailability(doctorId, date.toString())
                .onSuccess { slots ->
                    _state.update { it.copy(isLoadingSlots = false, slots = slots) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoadingSlots = false, error = err.toUserMessage()) }
                }
        }
    }
}
