package com.inclinic.app.features.patient.presentation.component

import com.arkivanov.decompose.value.Value

interface SettingsComponent {
    val state: Value<SettingsState>

    fun onPushToggle(enabled: Boolean)
    fun onAnalyticsToggle(enabled: Boolean)
    fun onChangePassword()
    fun onSubscribe()
    fun onBack()
    fun onDeleteAccount()

    sealed interface Output {
        data object Back : Output
        data object NavigateToChangePassword : Output
        data object NavigateToDeleteAccount : Output
    }
}

data class SettingsState(
    val email: String = "",
    val emailVerified: Boolean = false,
    val pushEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val isPremium: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)
