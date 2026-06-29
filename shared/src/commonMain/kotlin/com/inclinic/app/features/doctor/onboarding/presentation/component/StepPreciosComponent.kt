package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.features.doctor.onboarding.core.model.PriceConfig

data class StepPreciosState(
    val consultationFeeText: String = "",
    val supportsPresential: Boolean = true,
    val supportsVirtual: Boolean = false,
    val consultationFeeError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val canContinue: Boolean
        get() = consultationFeeText.toDoubleOrNull()?.let { it > 0.0 } == true &&
                (supportsPresential || supportsVirtual)
}

interface StepPreciosComponent {
    val state: Value<StepPreciosState>

    fun onConsultationFeeChanged(value: String)
    fun onTogglePresential(enabled: Boolean)
    fun onToggleVirtual(enabled: Boolean)
    fun onContinueClicked()
    fun onErrorDismissed()
}
