package com.inclinic.app.features.doctor.settings.presentation.component

import com.arkivanov.decompose.value.Value

interface DoctorSettingsComponent {
    val state: Value<DoctorSettingsState>

    fun onBack()
    fun onLogOut()
    fun onToggleNewAppointments(enabled: Boolean)
    fun onToggleChatMessages(enabled: Boolean)
    fun onToggleAppointmentReminders(enabled: Boolean)
    fun onToggleTwoFactor(enabled: Boolean)

    sealed interface Output {
        data object Back : Output
        data object LoggedOut : Output
    }
}

data class DoctorSettingsState(
    val newAppointmentsEnabled: Boolean = true,
    val chatMessagesEnabled: Boolean = true,
    val appointmentRemindersEnabled: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val isLoggingOut: Boolean = false,
)
