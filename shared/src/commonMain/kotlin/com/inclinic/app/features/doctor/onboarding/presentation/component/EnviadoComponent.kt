package com.inclinic.app.features.doctor.onboarding.presentation.component

import com.arkivanov.decompose.value.Value
import com.inclinic.app.core.model.OnboardingStatus

data class EnviadoState(
    val status: OnboardingStatus = OnboardingStatus.PENDING,
    val isLoading: Boolean = false,
    val error: String? = null,
)

interface EnviadoComponent {
    val state: Value<EnviadoState>

    sealed interface Output {
        data object LogOut : Output
    }

    fun onLogOutClicked()
    fun onErrorDismissed()
}
