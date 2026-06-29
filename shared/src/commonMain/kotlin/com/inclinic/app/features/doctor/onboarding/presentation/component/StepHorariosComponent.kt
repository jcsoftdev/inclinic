package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.onboarding.core.model.WeeklySchedule

data class StepHorariosState(
    /** Day name (e.g. "MONDAY") -> enabled flag */
    val enabledDays: Map<String, Boolean> = defaultEnabledDays(),
    /** Day name -> list of selected hour slots (8..20) */
    val slots: Map<String, List<Int>> = emptyMap(),
    val minNoticeHours: Int = 24,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canContinue: Boolean
        get() = enabledDays.any { (day, enabled) -> enabled && slots[day]?.isNotEmpty() == true }

    companion object {
        private val DAYS = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
        fun defaultEnabledDays(): Map<String, Boolean> = DAYS.associateWith { false }
    }
}

interface StepHorariosComponent {
    val state: Value<StepHorariosState>

    fun onToggleDay(day: String)
    fun onToggleSlot(day: String, hour: Int)
    fun onMinNoticeChanged(hours: Int)
    fun onContinueClicked()
    fun onErrorDismissed()
}
