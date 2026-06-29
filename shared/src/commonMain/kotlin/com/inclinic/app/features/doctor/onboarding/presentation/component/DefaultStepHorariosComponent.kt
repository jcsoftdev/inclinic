package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.inclinic.app.core.concurrency.AppDispatchers
import com.inclinic.app.features.doctor.onboarding.core.model.WeeklySchedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class DefaultStepHorariosComponent(
    componentContext: ComponentContext,
    private val dispatchers: AppDispatchers,
    private val onContinue: (WeeklySchedule) -> Unit,
) : StepHorariosComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(dispatchers.main + SupervisorJob())

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    private val _state = MutableValue(StepHorariosState())
    override val state: Value<StepHorariosState> = _state

    override fun onToggleDay(day: String) {
        _state.update { s ->
            val current = s.enabledDays[day] ?: false
            s.copy(
                enabledDays = s.enabledDays + (day to !current),
                error = null,
            )
        }
    }

    override fun onToggleSlot(day: String, hour: Int) {
        _state.update { s ->
            val current = s.slots[day] ?: emptyList()
            val updated = if (hour in current) current - hour else current + hour
            s.copy(slots = s.slots + (day to updated), error = null)
        }
    }

    override fun onMinNoticeChanged(hours: Int) {
        _state.update { it.copy(minNoticeHours = hours) }
    }

    override fun onContinueClicked() {
        if (!_state.value.canContinue) {
            _state.update { it.copy(error = "Habilita al menos un día con horarios") }
            return
        }
        val s = _state.value
        val activeSlots = s.enabledDays
            .filter { (_, enabled) -> enabled }
            .keys
            .associateWith { day -> s.slots[day] ?: emptyList() }
            .filter { (_, hours) -> hours.isNotEmpty() }

        onContinue(WeeklySchedule(slots = activeSlots, minNoticeHours = s.minNoticeHours))
    }

    override fun onErrorDismissed() {
        _state.update { it.copy(error = null) }
    }
}
